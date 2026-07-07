package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LeaderboardMenu {

    public static void open(SolarElo plugin, Player player, int page, String filter) {
        if (!plugin.getGuiConfigManager().getLeaderboardConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-leaderboard", "&#ff3c3cTính năng Bảng xếp hạng hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        if (filter == null || filter.isEmpty()) filter = "HIGH_TO_LOW";
        final String finalFilter = filter;
        org.bukkit.configuration.file.FileConfiguration guiConfig = plugin.getGuiConfigManager().getLeaderboardConfig();
        List<String> disposition = guiConfig.getStringList("gui-disposition");
        int tempRows = guiConfig.getInt("rows", 6);
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 6;
        final int rows = tempRows;

        int tempLimit = (rows - 1) * 9;
        if (disposition != null && !disposition.isEmpty()) {
            int count = 0;
            for (String row : disposition) {
                for (int c = 0; c < row.length() && c < 9; c++) {
                    if (row.charAt(c) == 'x') {
                        count++;
                    }
                }
            }
            tempLimit = count;
        }
        final int limit = tempLimit;
        final int offset = (page - 1) * limit;

        plugin.runAsync(() -> {
            List<PlayerData> players;
            int totalPlayers;

            if (finalFilter.equalsIgnoreCase("ONLINE_ONLY")) {
                List<PlayerData> onlineData = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEloManager().isIpBlocked(p)) {
                        continue;
                    }
                    PlayerData data = plugin.getEloManager().getCachedData(p.getUniqueId());
                    if (data != null) {
                        onlineData.add(data);
                    }
                }
                onlineData.sort((p1, p2) -> Integer.compare(p2.getElo(), p1.getElo()));
                totalPlayers = onlineData.size();
                players = new ArrayList<>();
                for (int i = offset; i < Math.min(onlineData.size(), offset + limit); i++) {
                    players.add(onlineData.get(i));
                }
            } else {
                boolean descending = !"LOW_TO_HIGH".equalsIgnoreCase(finalFilter);
                List<PlayerData> dbPlayers = plugin.getDatabaseManager().getTopPlayers(limit + 50, offset, descending);
                players = new ArrayList<>();
                for (PlayerData pd : dbPlayers) {
                    Player onlinePlayer = Bukkit.getPlayer(pd.getUuid());
                    if (onlinePlayer != null && plugin.getEloManager().isIpBlocked(onlinePlayer)) {
                        continue;
                    }
                    players.add(pd);
                    if (players.size() >= limit) {
                        break;
                    }
                }
                int blockedOnlineCount = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getEloManager().isIpBlocked(p)) {
                        blockedOnlineCount++;
                    }
                }
                totalPlayers = Math.max(0, plugin.getDatabaseManager().getTotalPlayers() - blockedOnlineCount);
            }

            int selfRank = plugin.getDatabaseManager().getPlayerRank(player.getUniqueId());
            plugin.getEloManager().updateRankCache(player.getUniqueId(), selfRank);
            PlayerData selfData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
            final int finalSelfRank = selfRank;
            final PlayerData finalSelfData = selfData;

            boolean hasNextPage = totalPlayers > (page * limit);

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                EloGui.LeaderboardHolder holder = new EloGui.LeaderboardHolder(page, finalFilter);
                String titleTemplate = guiConfig.getString("title", "#555555Bảng xếp hạng Elo - Trang {page}");
                String title = EloGui.colorize(titleTemplate.replace("{page}", String.valueOf(page)));
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                setupLeaderboardFiller(inv, guiConfig, disposition, rows, limit);

                List<Integer> lbSlots = null;
                if (disposition != null && !disposition.isEmpty()) {
                    lbSlots = EloGui.getSlotsFromLayout(guiConfig, 'x');
                }

                addLeaderboardPlayers(inv, plugin, guiConfig, players, lbSlots, offset);

                addLeaderboardControls(inv, plugin, guiConfig, disposition, page, finalFilter, rows, hasNextPage);

                addLeaderboardSelfHead(inv, plugin, guiConfig, disposition, rows, finalSelfData, finalSelfRank);

                player.openInventory(inv);
            });
        });
    }

    private static void setupLeaderboardFiller(Inventory inv, org.bukkit.configuration.file.FileConfiguration guiConfig, List<String> disposition, int rows, int limit) {
        boolean fillerEnabled = guiConfig.getBoolean("filler.enabled", false);
        if (fillerEnabled) {
            Material paneMat = EloGui.getMaterial(guiConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
            ItemStack pane = new ItemStack(paneMat);
            ItemMeta paneMeta = pane.getItemMeta();
            if (paneMeta != null) {
                paneMeta.setDisplayName(" ");
                pane.setItemMeta(paneMeta);
            }
            if (disposition != null && !disposition.isEmpty()) {
                for (int i = 0; i < rows * 9; i++) {
                    inv.setItem(i, pane);
                }
            } else {
                for (int i = limit; i < rows * 9; i++) {
                    inv.setItem(i, pane);
                }
            }
        }
    }

    private static void addLeaderboardPlayers(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration guiConfig, List<PlayerData> players, List<Integer> lbSlots, int offset) {
        int slot = 0;
        String headNameTemplate = guiConfig.getString("player-head.name", "&e#{pos} &f{player}");
        List<String> headLoreTemplate = guiConfig.getStringList("player-head.lore");
        if (headLoreTemplate.isEmpty()) {
            headLoreTemplate = Arrays.asList("#aaaaaaThứ hạng: &r{rank}", "#aaaaaaĐiểm Elo: #ffaa00{elo}", "", "#ffaa00Nhấp để xem chi tiết thống kê!");
        }

        int idx = 0;
        for (PlayerData pData : players) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                SkinsRestorerHook.applySkin(skullMeta, pData.getUuid(), pData.getName());

                org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "player_uuid");
                skullMeta.getPersistentDataContainer().set(uuidKey, org.bukkit.persistence.PersistentDataType.STRING, pData.getUuid().toString());

                String rankKey = plugin.getRankManager().getRank(pData.getElo());
                String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

                int pos = offset + idx + 1;
                String posColor = EloGui.getPositionColor(plugin, pos);

                skullMeta.setDisplayName(EloGui.colorize(headNameTemplate
                        .replace("{pos}", String.valueOf(pos))
                        .replace("{pos_color}", posColor)
                        .replace("{player}", pData.getName())));

                List<String> lore = new ArrayList<>();
                for (String line : headLoreTemplate) {
                    lore.add(EloGui.colorize(line
                            .replace("{rank}", rankDisplay)
                            .replace("{pos_color}", posColor)
                            .replace("{elo}", EloGui.formatNumber(pData.getElo()))
                            .replace("{kills}", EloGui.formatNumber(pData.getKills()))
                            .replace("{deaths}", EloGui.formatNumber(pData.getDeaths()))
                            .replace("{kd}", EloGui.formatNumber(pData.getKDRatio()))
                            .replace("{streak}", EloGui.formatNumber(pData.getCurrentStreak()))
                            .replace("{best_streak}", EloGui.formatNumber(pData.getBestStreak()))
                    ));
                }

                skullMeta.setLore(lore);
                head.setItemMeta(skullMeta);
            }
            if (lbSlots != null) {
                if (idx < lbSlots.size()) {
                    inv.setItem(lbSlots.get(idx), head);
                }
            } else {
                inv.setItem(slot++, head);
            }
            idx++;
        }
    }

    private static void addLeaderboardControls(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration guiConfig, List<String> disposition, int page, String finalFilter, int rows, boolean hasNextPage) {
        int prevSlot;
        if (disposition != null && !disposition.isEmpty()) {
            prevSlot = EloGui.getSlotFromLayout(guiConfig, 'b', 45);
        } else {
            prevSlot = guiConfig.contains("back.slot") ? guiConfig.getInt("back.slot") : guiConfig.getInt("previous-page.slot", 45);
        }
        if (prevSlot >= 0 && prevSlot < rows * 9) {
            if (page > 1) {
                String section = guiConfig.contains("back") ? "back" : "previous-page";
                Material mat = EloGui.getMaterial(guiConfig.getString(section + ".material"), Material.ARROW);
                ItemStack prev = new ItemStack(mat);
                ItemMeta prevMeta = prev.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.setDisplayName(EloGui.colorize(guiConfig.getString(section + ".name", "#00BFFF\u029c\u1d00\u1d04\u1d0b")));
                    List<String> prevLore = new ArrayList<>();
                    for (String l : guiConfig.getStringList(section + ".lore")) {
                        prevLore.add(EloGui.colorize(l.replace("{page}", String.valueOf(page - 1))));
                    }
                    prevMeta.setLore(prevLore);
                    int cmd = guiConfig.getInt(section + ".customModelData", -1);
                    if (cmd != -1) {
                        prevMeta.setCustomModelData(cmd);
                    }
                    prev.setItemMeta(prevMeta);
                }
                inv.setItem(prevSlot, prev);
            } else {
                Material mat = EloGui.getMaterial(guiConfig.getString("back.material"), Material.ARROW);
                ItemStack prev = new ItemStack(mat);
                ItemMeta prevMeta = prev.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.setDisplayName(EloGui.colorize("#00BFFF\u029c\u1d00\u1d04\u1d0b"));
                    List<String> prevLore = new ArrayList<>();
                    prevLore.add(EloGui.colorize("&fClick to return to the main menu"));
                    prevMeta.setLore(prevLore);
                    int cmd = guiConfig.getInt("back.customModelData", -1);
                    if (cmd != -1) {
                        prevMeta.setCustomModelData(cmd);
                    }
                    prev.setItemMeta(prevMeta);
                }
                inv.setItem(prevSlot, prev);
            }
        }

        int refSlot;
        if (disposition != null && !disposition.isEmpty()) {
            refSlot = EloGui.getSlotFromLayout(guiConfig, 'r', 49);
        } else {
            refSlot = guiConfig.getInt("refresh.slot", 49);
        }
        if (refSlot >= 0 && refSlot < rows * 9) {
            Material mat = EloGui.getMaterial(guiConfig.getString("refresh.material"), Material.ANVIL);
            ItemStack refreshItem = new ItemStack(mat);
            ItemMeta refMeta = refreshItem.getItemMeta();
            if (refMeta != null) {
                refMeta.setDisplayName(EloGui.colorize(guiConfig.getString("refresh.name", "#00BFFF\u0280\u1d07\u0254\u0280\u1d07s\u029c")));
                List<String> refLore = new ArrayList<>();
                for (String l : guiConfig.getStringList("refresh.lore")) {
                    refLore.add(EloGui.colorize(l));
                }
                refMeta.setLore(refLore);
                int cmd = guiConfig.getInt("refresh.customModelData", -1);
                if (cmd != -1) {
                    refMeta.setCustomModelData(cmd);
                }
                refreshItem.setItemMeta(refMeta);
            }
            inv.setItem(refSlot, refreshItem);
        }

        int filSlot;
        if (disposition != null && !disposition.isEmpty()) {
            filSlot = EloGui.getSlotFromLayout(guiConfig, 'f', 50);
        } else {
            filSlot = guiConfig.getInt("filter.slot", 50);
        }
        if (filSlot >= 0 && filSlot < rows * 9) {
            Material mat = EloGui.getMaterial(guiConfig.getString("filter.material"), Material.HOPPER);
            ItemStack filterItem = new ItemStack(mat);
            ItemMeta filMeta = filterItem.getItemMeta();
            if (filMeta != null) {
                filMeta.setDisplayName(EloGui.colorize(guiConfig.getString("filter.name", "#00BFFF\u0252\u026a\u029f\u1d1b\u1d07\u0280")));
                List<String> filLore = new ArrayList<>();

                List<String> options = guiConfig.getStringList("filter.options");
                if (options == null || options.isEmpty()) {
                    options = Arrays.asList("HIGH_TO_LOW", "LOW_TO_HIGH", "ONLINE_ONLY");
                }
                String selectedColor = guiConfig.getString("filter.selected_color", "#00BFFF");
                String unselectedColor = guiConfig.getString("filter.unselected_color", "#ffffff");
                String bulletIcon = guiConfig.getString("filter.bullet_icon", "\u25aa ");

                List<String> optionLines = new ArrayList<>();
                for (String opt : options) {
                    boolean isCurrent = opt.equalsIgnoreCase(finalFilter);

                    String optNameKey = "filter.option-names." + opt;
                    String optName = guiConfig.getString(optNameKey);
                    if (optName == null) {
                        if (opt.equalsIgnoreCase("HIGH_TO_LOW")) {
                            optName = "High to Low (Top)";
                        } else if (opt.equalsIgnoreCase("LOW_TO_HIGH")) {
                            optName = "Low to High (Bottom)";
                        } else if (opt.equalsIgnoreCase("ONLINE_ONLY")) {
                            optName = "Online Only (Top ELO)";
                        } else {
                            optName = opt;
                        }
                    }

                    String color = isCurrent ? selectedColor : unselectedColor;
                    optionLines.add(EloGui.colorize(color + bulletIcon + optName));
                }

                for (String l : guiConfig.getStringList("filter.lore")) {
                    if (l.contains("{options}")) {
                        filLore.addAll(optionLines);
                    } else {
                        filLore.add(EloGui.colorize(l));
                    }
                }
                filMeta.setLore(filLore);
                int cmd = guiConfig.getInt("filter.customModelData", -1);
                if (cmd != -1) {
                    filMeta.setCustomModelData(cmd);
                }
                filterItem.setItemMeta(filMeta);
            }
            inv.setItem(filSlot, filterItem);
        }

        int nextSlot;
        if (disposition != null && !disposition.isEmpty()) {
            nextSlot = EloGui.getSlotFromLayout(guiConfig, 'n', 53);
        } else {
            nextSlot = guiConfig.contains("next.slot") ? guiConfig.getInt("next.slot") : guiConfig.getInt("next-page.slot", 53);
        }
        if (hasNextPage && nextSlot >= 0 && nextSlot < rows * 9) {
            String section = guiConfig.contains("next") ? "next" : "next-page";
            Material mat = EloGui.getMaterial(guiConfig.getString(section + ".material"), Material.ARROW);
            ItemStack nextItem = new ItemStack(mat);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(EloGui.colorize(guiConfig.getString(section + ".name", "#00BFFF\u0274\u1d07x\u1d1b")));
                List<String> nextLore = new ArrayList<>();
                for (String l : guiConfig.getStringList(section + ".lore")) {
                    nextLore.add(EloGui.colorize(l.replace("{page}", String.valueOf(page + 1))));
                }
                nextMeta.setLore(nextLore);
                int cmd = guiConfig.getInt(section + ".customModelData", -1);
                if (cmd != -1) {
                    nextMeta.setCustomModelData(cmd);
                }
                nextItem.setItemMeta(nextMeta);
            }
            inv.setItem(nextSlot, nextItem);
        }
    }

    private static void addLeaderboardSelfHead(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration guiConfig, List<String> disposition, int rows, PlayerData finalSelfData, int finalSelfRank) {
        boolean selfHeadEnabled = guiConfig.getBoolean("self-player-head.enabled", false);
        int selfHeadSlot;
        if (disposition != null && !disposition.isEmpty()) {
            selfHeadSlot = EloGui.getSlotFromLayout(guiConfig, 's', 48);
        } else {
            selfHeadSlot = guiConfig.getInt("self-player-head.slot", 47);
        }
        if (selfHeadEnabled && selfHeadSlot >= 0 && selfHeadSlot < rows * 9 && finalSelfData != null) {
            ItemStack selfHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) selfHead.getItemMeta();
            if (skullMeta != null) {
                SkinsRestorerHook.applySkin(skullMeta, finalSelfData.getUuid(), finalSelfData.getName());

                String selfNameTemplate = guiConfig.getString("self-player-head.name", "#00BFFFsᴇʟꜰ #ffffff{player}");
                List<String> selfLoreTemplate = guiConfig.getStringList("self-player-head.lore");
                if (selfLoreTemplate.isEmpty()) {
                    selfLoreTemplate = Arrays.asList("&fCurrent Elo: #ffaa00{elo}", "&fCurrent Rank: {pos_color}#{pos}");
                }

                String rankKey = plugin.getRankManager().getRank(finalSelfData.getElo());
                String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);
                String selfRankStr = finalSelfRank <= 0 ? "N/A" : String.valueOf(finalSelfRank);

                String selfPosColor = EloGui.getPositionColor(plugin, finalSelfRank);

                skullMeta.setDisplayName(EloGui.colorize(selfNameTemplate
                        .replace("{pos_color}", selfPosColor)
                        .replace("{player}", finalSelfData.getName())));

                String seasonTime = plugin.getEloManager().getSeasonTimeRemaining();
                String nextRankKey = plugin.getRankManager().getNextRank(finalSelfData.getElo());
                String nextRankDisplay = nextRankKey != null ? plugin.getRankManager().getRankDisplay(nextRankKey) : EloGui.colorize("&cTối đa");
                int eloNeeded = plugin.getRankManager().getEloNeededForNextRank(finalSelfData.getElo());
                String eloNeededStr = nextRankKey != null ? EloGui.formatNumber(eloNeeded) : "0";

                List<String> lore = new ArrayList<>();
                for (String line : selfLoreTemplate) {
                    lore.add(EloGui.colorize(line
                            .replace("{rank}", rankDisplay)
                            .replace("{pos_color}", selfPosColor)
                            .replace("{elo}", EloGui.formatNumber(finalSelfData.getElo()))
                            .replace("{pos}", selfRankStr)
                            .replace("{kills}", EloGui.formatNumber(finalSelfData.getKills()))
                            .replace("{deaths}", EloGui.formatNumber(finalSelfData.getDeaths()))
                            .replace("{kd}", EloGui.formatNumber(finalSelfData.getKDRatio()))
                            .replace("{streak}", EloGui.formatNumber(finalSelfData.getCurrentStreak()))
                            .replace("{best_streak}", EloGui.formatNumber(finalSelfData.getBestStreak()))
                            .replace("{season_time}", seasonTime)
                            .replace("{next_rank}", nextRankDisplay)
                            .replace("{elo_needed}", eloNeededStr)
                    ));
                }

                skullMeta.setLore(lore);
                int cmd = guiConfig.getInt("self-player-head.customModelData", -1);
                if (cmd != -1) {
                    skullMeta.setCustomModelData(cmd);
                }
                selfHead.setItemMeta(skullMeta);
            }
            inv.setItem(selfHeadSlot, selfHead);
        }
    }

    public static void handleInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.LeaderboardHolder leaderboardHolder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration guiConfig = plugin.getGuiConfigManager().getLeaderboardConfig();
        int defaultPrevSlot = guiConfig.contains("back.slot") ? guiConfig.getInt("back.slot") : guiConfig.getInt("previous-page.slot", 45);
        int prevSlot = EloGui.getSlotFromLayout(guiConfig, 'b', defaultPrevSlot);
        int filSlot = EloGui.getSlotFromLayout(guiConfig, 'f', guiConfig.getInt("filter.slot", 50));
        int refreshSlot = EloGui.getSlotFromLayout(guiConfig, 'r', guiConfig.getInt("refresh.slot", 49));
        int defaultNextSlot = guiConfig.contains("next.slot") ? guiConfig.getInt("next.slot") : guiConfig.getInt("next-page.slot", 53);
        int nextSlot = EloGui.getSlotFromLayout(guiConfig, 'n', defaultNextSlot);

        int page = leaderboardHolder.getPage();
        String filter = leaderboardHolder.getFilter();

        String backSection = guiConfig.contains("back") ? "back" : "previous-page";
        String nextSection = guiConfig.contains("next") ? "next" : "next-page";

        ItemStack currentItem = event.getCurrentItem();
        if (slot == prevSlot || slot == filSlot || slot == refreshSlot || slot == nextSlot) {
            handleLeaderboardNavigation(player, slot, prevSlot, filSlot, refreshSlot, nextSlot, page, filter, guiConfig, backSection, nextSection, currentItem, plugin);
        } else if (currentItem != null && currentItem.getType() == Material.PLAYER_HEAD) {
            handleLeaderboardHeadClick(player, currentItem, page, filter, guiConfig, plugin);
        }
    }

    private static void handleLeaderboardNavigation(Player player, int slot, int prevSlot, int filSlot, int refreshSlot, int nextSlot, int page, String filter, org.bukkit.configuration.file.FileConfiguration guiConfig, String backSection, String nextSection, ItemStack currentItem, SolarElo plugin) {
        if (slot == prevSlot && currentItem != null && currentItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            String soundKey = guiConfig.getString(backSection + ".confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);
            if (page > 1) {
                EloGui.openLeaderboard(plugin, player, page - 1, filter);
            } else {
                EloGui.openMainMenu(plugin, player);
            }
        } else if (slot == filSlot && currentItem != null && currentItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            String soundKey = guiConfig.getString("filter.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);

            java.util.List<String> options = guiConfig.getStringList("filter.options");
            if (options.isEmpty()) {
                options = java.util.Arrays.asList("HIGH_TO_LOW", "LOW_TO_HIGH", "ONLINE_ONLY");
            }
            int currentIndex = options.indexOf(filter);
            int nextIndex = 0;
            if (currentIndex != -1) {
                nextIndex = (currentIndex + 1) % options.size();
            }
            String nextFilter = options.get(nextIndex);
            EloGui.openLeaderboard(plugin, player, page, nextFilter);
        } else if (slot == refreshSlot && currentItem != null && currentItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            String soundKey = guiConfig.getString("refresh.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);
            EloGui.openLeaderboard(plugin, player, page, filter);
        } else if (slot == nextSlot && currentItem != null && currentItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            String soundKey = guiConfig.getString(nextSection + ".confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);
            EloGui.openLeaderboard(plugin, player, page + 1, filter);
        }
    }

    private static void handleLeaderboardHeadClick(Player player, ItemStack currentItem, int page, String filter, org.bukkit.configuration.file.FileConfiguration guiConfig, SolarElo plugin) {
        org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) currentItem.getItemMeta();
        if (skullMeta == null) return;

        org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "player_uuid");
        UUID targetUuid = null;
        if (skullMeta.getPersistentDataContainer().has(uuidKey, org.bukkit.persistence.PersistentDataType.STRING)) {
            String str = skullMeta.getPersistentDataContainer().get(uuidKey, org.bukkit.persistence.PersistentDataType.STRING);
            if (str != null) targetUuid = UUID.fromString(str);
        }
        if (targetUuid == null && skullMeta.getOwningPlayer() != null) {
            targetUuid = skullMeta.getOwningPlayer().getUniqueId();
        }
        if (targetUuid != null) {
            org.bukkit.OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetUuid);
            if (plugin.getGuiConfigManager().getStatsConfig().getBoolean("enabled", true)) {
                String soundKey = guiConfig.getString("player-head.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openStats(plugin, player, target.getName() != null ? target.getName() : "Unknown", page, filter);
            } else {
                plugin.getEffectManager().playGuiSound(player, "error");
                player.closeInventory();
                final UUID finalTargetUuid = target.getUniqueId();
                final String finalTargetName = target.getName() != null ? target.getName() : "Unknown";
                plugin.runAsync(() -> {
                    PlayerData pData = plugin.getDatabaseManager().loadPlayer(finalTargetUuid, finalTargetName);

                    if (pData != null) {
                        String rankKey = plugin.getRankManager().getRank(pData.getElo());
                        String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

                        String detailHeader = plugin.getMessageManager().get("gui-detail-header", "&8&m         &r &eThống kê chi tiết &r&8&m         ");
                        String detailFormat = plugin.getMessageManager().get("gui-detail-format",
                            "&7Người chơi: &e{player}\n&7Cấp bậc: &r{rank}\n&7Điểm Elo: &e{elo}\n&7Mạng giết: &a{kills}\n&7Số lần chết: &c{deaths}\n&7Tỷ lệ K/D: &e{kd}\n&7Chuỗi thắng: &c{streak}\n&7Chuỗi thắng lớn nhất: &b{best_streak}"
                        );
                        String detailFooter = plugin.getMessageManager().get("gui-detail-footer", "&8&m                                           ");

                        String formattedMsg = detailFormat
                                .replace("{player}", finalTargetName)
                                .replace("{rank}", rankDisplay)
                                .replace("{elo}", String.valueOf(pData.getElo()))
                                .replace("{kills}", String.valueOf(pData.getKills()))
                                .replace("{deaths}", String.valueOf(pData.getDeaths()))
                                .replace("{kd}", String.valueOf(pData.getKDRatio()))
                                .replace("{streak}", String.valueOf(pData.getCurrentStreak()))
                                .replace("{best_streak}", String.valueOf(pData.getBestStreak()));

                        plugin.runForEntity(player, () -> {
                            player.sendMessage(EloGui.colorize(detailHeader));
                            for (String line : formattedMsg.split("\n")) {
                                player.sendMessage(EloGui.colorize(line));
                            }
                            player.sendMessage(EloGui.colorize(detailFooter));
                        });
                    }
                });
            }
        }
    }
}
