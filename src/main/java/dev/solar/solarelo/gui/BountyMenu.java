package dev.solar.solarelo.gui;
import dev.solar.solarelo.hooks.SkinsRestorerHook;

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

public class BountyMenu {

    public static void open(SolarElo plugin, Player player) {
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        open(plugin, player, 1, "HIGH_TO_LOW");
    }

    public static void open(SolarElo plugin, Player player, int page, String filter) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        String activeFilter = (filter == null || filter.isEmpty()) ? "HIGH_TO_LOW" : filter;
        org.bukkit.configuration.file.FileConfiguration bountyConfig = plugin.getGuiConfigManager().getBountyConfig();
        List<String> disposition = bountyConfig.getStringList("gui-disposition");
        GuiLayoutHelper.LayoutInfo layout = GuiLayoutHelper.getLayoutInfo(bountyConfig, "gui-disposition", page);
        int rows = layout.rows;
        int limit = layout.limit;
        int offset = layout.offset;

        plugin.runAsync(() -> {
            int selfRank = plugin.getDatabaseManager().getPlayerRank(player.getUniqueId());
            PlayerData cached = plugin.getEloManager().getCachedData(player.getUniqueId());
            PlayerData selfData = cached != null ? cached : plugin.getDatabaseManager().loadPlayer(player.getUniqueId(), player.getName());

            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            onlinePlayers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));

            int minTargetElo = bountyConfig.getInt("minimum-target-elo", 5000);

            List<PlayerData> targetDataList = new ArrayList<>();
            for (Player p : onlinePlayers) {
                if (plugin.getEloManager().isIpBlocked(p)) {
                    continue;
                }
                PlayerData data = plugin.getEloManager().getCachedData(p.getUniqueId());
                if (data == null) {
                    data = plugin.getEloManager().getData(p.getUniqueId(), p.getName());
                }
                if (data != null) {
                    if (data.getElo() >= minTargetElo && !plugin.getEloManager().isBountyTargetActive(p.getUniqueId())) {
                        targetDataList.add(data);
                    }
                }
            }

            if (activeFilter.equalsIgnoreCase("LOW_TO_HIGH")) {
                targetDataList.sort((d1, d2) -> Integer.compare(d1.getElo(), d2.getElo()));
            } else {
                targetDataList.sort((d1, d2) -> Integer.compare(d2.getElo(), d1.getElo()));
            }

            int totalTargets = targetDataList.size();
            List<PlayerData> paginatedTargets = new ArrayList<>();
            int safeOffset = Math.max(0, offset);
            if (safeOffset < totalTargets) {
                for (int i = safeOffset; i < Math.min(totalTargets, safeOffset + limit); i++) {
                    paginatedTargets.add(targetDataList.get(i));
                }
            }
            boolean hasNextPage = totalTargets > (page * limit);

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                long cooldownEnd = plugin.getEloManager().getBountyCooldown(player.getUniqueId());
                long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
                UUID activeTargetUuid = plugin.getEloManager().getActiveBountyTarget(player.getUniqueId());

                String titleTemplate = bountyConfig.getString("title", "#ff3c3cBounty Quests");
                String activeTargetName = "";
                String timeRemainingStr = "";

                if (activeTargetUuid != null) {
                    org.bukkit.OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(activeTargetUuid);
                    activeTargetName = targetPlayer.getName() != null ? targetPlayer.getName() : "Unknown";
                    long activeEnd = plugin.getEloManager().getActiveBountyEndTime(player.getUniqueId());
                    long activeRemaining = (activeEnd - System.currentTimeMillis()) / 1000;
                    timeRemainingStr = EloGui.formatTimeRemaining(activeRemaining);
                    titleTemplate = bountyConfig.getString("active-title", titleTemplate);
                } else if (remaining > 0) {
                    timeRemainingStr = EloGui.formatTimeRemaining(remaining);
                }

                String title = EloGui.colorize(titleTemplate
                        .replace("{page}", String.valueOf(page))
                        .replace("{target}", activeTargetName)
                        .replace("{time_remaining}", timeRemainingStr)
                        .replace("{remaining}", String.valueOf(Math.max(0, remaining))));

                EloGui.BountyHolder holder = new EloGui.BountyHolder(page, activeFilter);
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                setupBountyLayout(inv, plugin, bountyConfig, disposition, rows, limit, selfData, paginatedTargets);

                boolean isLocked = selfData.getElo() < bountyConfig.getInt("minimum-unlock-elo", 1200);
                addBountyControls(inv, plugin, bountyConfig, page, activeFilter, rows, hasNextPage, isLocked, activeTargetUuid, remaining);

                player.openInventory(inv);
            });
        });
    }

    private static void setupBountyLayout(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, List<String> disposition, int rows, int limit, PlayerData selfData, List<PlayerData> paginatedTargets) {
        boolean fillerEnabled = bountyConfig.getBoolean("filler.enabled", false);
        if (fillerEnabled) {
            Material paneMat = EloGui.getMaterial(bountyConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
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

        boolean isLocked = selfData.getElo() < bountyConfig.getInt("minimum-unlock-elo", 1200);

        if (isLocked) {
            int slot = EloGui.getSlotFromLayout(bountyConfig, 'l', bountyConfig.getInt("locked-item.slot", 22));
            if (slot >= 0 && slot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("locked-item.material"), Material.BARRIER);
                ItemStack lockedItem = new ItemStack(mat);
                ItemMeta meta = lockedItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(EloGui.colorize(bountyConfig.getString("locked-item.name", "#ff3c3c\ud83d\udd12 \u029c\u1d0f\u1d1c\u0274\u1d1b\u028f \u029f\u1d0f\u1d04\u1d0b\u1d07\ud83d\udd12")));
                    List<String> lore = new ArrayList<>();
                    int reqElo = bountyConfig.getInt("minimum-unlock-elo", 1200);
                    for (String l : bountyConfig.getStringList("locked-item.lore")) {
                        lore.add(EloGui.colorize(l.replace("{required}", String.valueOf(reqElo))
                                           .replace("{elo}", String.valueOf(selfData.getElo()))));
                    }
                    meta.setLore(lore);
                    lockedItem.setItemMeta(meta);
                }
                inv.setItem(slot, lockedItem);
            }
        } else {
            if (paginatedTargets.isEmpty()) {
                int slot = EloGui.getSlotFromLayout(bountyConfig, 'i', bountyConfig.getInt("no-targets-item.slot", 22));
                if (slot >= 0 && slot < rows * 9) {
                    Material mat = EloGui.getMaterial(bountyConfig.getString("no-targets-item.material"), Material.BARRIER);
                    ItemStack noTargetsItem = new ItemStack(mat);
                    ItemMeta meta = noTargetsItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(EloGui.colorize(bountyConfig.getString("no-targets-item.name", "#ff3c3c\u0274\u1d0f \u1d20\u1d00\u029f\u026a\u1d05 \u1d1b\u1d00\u0280\u0262\u1d07\u1d1bs")));
                        List<String> lore = new ArrayList<>();
                        for (String l : bountyConfig.getStringList("no-targets-item.lore")) {
                            lore.add(EloGui.colorize(l));
                        }
                        meta.setLore(lore);
                        noTargetsItem.setItemMeta(meta);
                    }
                    inv.setItem(slot, noTargetsItem);
                }
            } else {
                addBountyTargets(inv, plugin, bountyConfig, paginatedTargets, limit);
            }
        }
    }

    private static void addBountyTargets(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, List<PlayerData> paginatedTargets, int limit) {
        int targetSlot = 0;
        int rewardElo = plugin.getBountyConfig().getInt("bounty-quest.reward-elo", 20);
        String headNameTemplate = bountyConfig.getString("target-player-head.name", "#ff3c3c\u1d1b\u1d00\u0280\u0262\u1d07\u1d1b #ffffff{player}");
        List<String> headLoreTemplate = bountyConfig.getStringList("target-player-head.lore");

        for (PlayerData tData : paginatedTargets) {
            if (targetSlot >= limit) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                SkinsRestorerHook.applySkin(skullMeta, tData.getUuid(), tData.getName());

                org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "target_uuid");
                skullMeta.getPersistentDataContainer().set(uuidKey, org.bukkit.persistence.PersistentDataType.STRING, tData.getUuid().toString());

                String rankKey = plugin.getRankManager().getRank(tData.getElo());
                String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

                skullMeta.setDisplayName(EloGui.colorize(headNameTemplate.replace("{player}", tData.getName())));
                List<String> lore = new ArrayList<>();
                for (String l : headLoreTemplate) {
                    lore.add(EloGui.colorize(l.replace("{elo}", EloGui.formatNumber(tData.getElo()))
                                       .replace("{rank}", rankDisplay)
                                       .replace("{reward_elo}", EloGui.formatNumber(rewardElo))));
                }
                skullMeta.setLore(lore);
                head.setItemMeta(skullMeta);
            }
            inv.setItem(targetSlot++, head);
        }
    }

    private static void addBountyControls(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, int page, String activeFilter, int rows, boolean hasNextPage, boolean isLocked, UUID activeTargetUuid, long remaining) {
        int prevSlot = EloGui.getSlotFromLayout(bountyConfig, 'b', bountyConfig.getInt("back.slot", 45));
        if (prevSlot >= 0 && prevSlot < rows * 9) {
            if (page > 1) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("back.material"), Material.ARROW);
                ItemStack prev = new ItemStack(mat);
                ItemMeta prevMeta = prev.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("back.name", "#00BFFF\u029c\u1d00\u1d04\u1d0b")));
                    List<String> prevLore = new ArrayList<>();
                    for (String l : bountyConfig.getStringList("back.lore")) {
                        prevLore.add(EloGui.colorize(l.replace("{page}", String.valueOf(page - 1))));
                    }
                    prevMeta.setLore(prevLore);
                    int cmd = bountyConfig.getInt("back.customModelData", -1);
                    if (cmd != -1) {
                        prevMeta.setCustomModelData(cmd);
                    }
                    prev.setItemMeta(prevMeta);
                }
                inv.setItem(prevSlot, prev);
            } else {
                Material mat = EloGui.getMaterial(bountyConfig.getString("back.material"), Material.ARROW);
                ItemStack prev = new ItemStack(mat);
                ItemMeta prevMeta = prev.getItemMeta();
                if (prevMeta != null) {
                    prevMeta.setDisplayName(EloGui.colorize("#00BFFF\u029c\u1d00\u1d04\u1d0b"));
                    List<String> lore = new ArrayList<>();
                    lore.add(EloGui.colorize("&fClick to return to the main menu"));
                    prevMeta.setLore(lore);
                    int cmd = bountyConfig.getInt("back.customModelData", -1);
                    if (cmd != -1) {
                        prevMeta.setCustomModelData(cmd);
                    }
                    prev.setItemMeta(prevMeta);
                }
                inv.setItem(prevSlot, prev);
            }
        }

        if (!isLocked) {
            int nextSlot = EloGui.getSlotFromLayout(bountyConfig, 'n', bountyConfig.getInt("next.slot", 53));
            if (hasNextPage && nextSlot >= 0 && nextSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("next.material"), Material.ARROW);
                ItemStack nextItem = new ItemStack(mat);
                ItemMeta nextMeta = nextItem.getItemMeta();
                if (nextMeta != null) {
                    nextMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("next.name", "#00BFFF\u0274\u1d07x\u1d1b")));
                    List<String> nextLore = new ArrayList<>();
                    for (String l : bountyConfig.getStringList("next.lore")) {
                        nextLore.add(EloGui.colorize(l.replace("{page}", String.valueOf(page + 1))));
                    }
                    nextMeta.setLore(nextLore);
                    int cmd = bountyConfig.getInt("next.customModelData", -1);
                    if (cmd != -1) {
                        nextMeta.setCustomModelData(cmd);
                    }
                    nextItem.setItemMeta(nextMeta);
                }
                inv.setItem(nextSlot, nextItem);
            }

            int activeQuestSlot = EloGui.getSlotFromLayout(bountyConfig, 'a', bountyConfig.getInt("active-quest.slot", 48));
            if (activeQuestSlot >= 0 && activeQuestSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("active-quest.material"), Material.BLUE_BANNER);
                ItemStack activeQuestItem = new ItemStack(mat);
                ItemMeta activeQuestMeta = activeQuestItem.getItemMeta();
                if (activeQuestMeta != null) {
                    activeQuestMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("active-quest.name", "#00BFFF\u1d00\u1d04\u1d1b\u026a\u1d20\u1d07 \u0281\u1d1c\u1d07s\u1d1b")));
                    List<String> activeQuestLore = new ArrayList<>();
                    for (String l : bountyConfig.getStringList("active-quest.lore")) {
                        activeQuestLore.add(EloGui.colorize(l));
                    }
                    activeQuestMeta.setLore(activeQuestLore);
                    int cmd = bountyConfig.getInt("active-quest.customModelData", -1);
                    if (cmd != -1) {
                        activeQuestMeta.setCustomModelData(cmd);
                    }
                    activeQuestItem.setItemMeta(activeQuestMeta);
                }
                inv.setItem(activeQuestSlot, activeQuestItem);
            }

            int refSlot = EloGui.getSlotFromLayout(bountyConfig, 'r', bountyConfig.getInt("refresh.slot", 49));
            if (refSlot >= 0 && refSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("refresh.material"), Material.ANVIL);
                ItemStack refreshItem = new ItemStack(mat);
                ItemMeta refMeta = refreshItem.getItemMeta();
                if (refMeta != null) {
                    refMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("refresh.name", "#00BFFF\u0280\u1d07\u0254\u0280\u1d07s\u029c")));
                    List<String> refLore = new ArrayList<>();
                    for (String l : bountyConfig.getStringList("refresh.lore")) {
                        refLore.add(EloGui.colorize(l));
                    }
                    refMeta.setLore(refLore);
                    int cmd = bountyConfig.getInt("refresh.customModelData", -1);
                    if (cmd != -1) {
                        refMeta.setCustomModelData(cmd);
                    }
                    refreshItem.setItemMeta(refMeta);
                }
                inv.setItem(refSlot, refreshItem);
            }

            int filSlot = EloGui.getSlotFromLayout(bountyConfig, 'f', bountyConfig.getInt("filter.slot", 50));
            if (filSlot >= 0 && filSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("filter.material"), Material.HOPPER);
                ItemStack filterItem = new ItemStack(mat);
                ItemMeta filMeta = filterItem.getItemMeta();
                if (filMeta != null) {
                    filMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("filter.name", "#00BFFF\u0252\u026a\u029f\u1d1b\u1d07\u0280")));
                    List<String> filLore = new ArrayList<>();

                    List<String> options = bountyConfig.getStringList("filter.options");
                    if (options == null || options.isEmpty()) {
                        options = Arrays.asList("HIGH_TO_LOW", "LOW_TO_HIGH");
                    }
                    String selectedColor = bountyConfig.getString("filter.selected_color", "#00BFFF");
                    String unselectedColor = bountyConfig.getString("filter.unselected_color", "&f");
                    String bulletIcon = bountyConfig.getString("filter.bullet_icon", "\u25aa ");

                    List<String> optionLines = new ArrayList<>();
                    for (String opt : options) {
                        boolean isCurrent = opt.equalsIgnoreCase(activeFilter);

                        String optNameKey = "filter.option-names." + opt;
                        String optName = bountyConfig.getString(optNameKey);
                        if (optName == null) {
                            if (opt.equalsIgnoreCase("HIGH_TO_LOW")) {
                                optName = "High to Low (Top)";
                            } else if (opt.equalsIgnoreCase("LOW_TO_HIGH")) {
                                optName = "Low to High (Bottom)";
                            } else {
                                optName = opt;
                            }
                        }

                        String color = isCurrent ? selectedColor : unselectedColor;
                        optionLines.add(EloGui.colorize(color + bulletIcon + optName));
                    }

                    for (String l : bountyConfig.getStringList("filter.lore")) {
                        if (l.contains("{options}")) {
                            filLore.addAll(optionLines);
                        } else {
                            filLore.add(EloGui.colorize(l));
                        }
                    }
                    filMeta.setLore(filLore);
                    int cmd = bountyConfig.getInt("filter.customModelData", -1);
                    if (cmd != -1) {
                        filMeta.setCustomModelData(cmd);
                    }
                    filterItem.setItemMeta(filMeta);
                }
                inv.setItem(filSlot, filterItem);
            }
        }
    }

    public static void handleInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.BountyHolder bountyHolder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration bountyConfig = plugin.getGuiConfigManager().getBountyConfig();
        java.util.UUID activeTarget = plugin.getEloManager().getActiveBountyTarget(player.getUniqueId());
        long cooldownEnd = plugin.getEloManager().getBountyCooldown(player.getUniqueId());
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;

        int page = bountyHolder.getPage();
        String filter = bountyHolder.getFilter();

        int prevSlot = EloGui.getSlotFromLayout(bountyConfig, 'b', bountyConfig.getInt("back.slot", 45));
        int nextSlot = EloGui.getSlotFromLayout(bountyConfig, 'n', bountyConfig.getInt("next.slot", 53));
        int activeQuestSlot = EloGui.getSlotFromLayout(bountyConfig, 'a', bountyConfig.getInt("active-quest.slot", 48));
        int refreshSlot = EloGui.getSlotFromLayout(bountyConfig, 'r', bountyConfig.getInt("refresh.slot", 49));
        int filSlot = EloGui.getSlotFromLayout(bountyConfig, 'f', bountyConfig.getInt("filter.slot", 50));

        PlayerData selfData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
        if (selfData != null && selfData.isLocked()) {
            plugin.getEffectManager().playGuiSound(player, "error");
            String msg = plugin.getMessageManager().get("bounty-locked-error", "&cElo của bạn đang bị khóa, không thể sử dụng tính năng săn thưởng!");
            player.sendMessage(EloGui.colorize(msg));
            player.closeInventory();
            return;
        }
        int minUnlockElo = bountyConfig.getInt("minimum-unlock-elo", 1200);
        if (selfData.getElo() < minUnlockElo) {
            if (slot == prevSlot) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openMainMenu(plugin, player);
            }
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (slot == prevSlot || slot == nextSlot || slot == activeQuestSlot || slot == refreshSlot || slot == filSlot) {
            handleBountyNavigation(player, slot, prevSlot, nextSlot, activeQuestSlot, refreshSlot, filSlot, page, filter, bountyConfig, currentItem, plugin);
        } else if (currentItem != null && currentItem.getType() == Material.PLAYER_HEAD) {
            handleBountyHeadClick(player, currentItem, activeTarget, remaining, bountyConfig, plugin);
        }
    }

    private static void handleBountyNavigation(Player player, int slot, int prevSlot, int nextSlot, int activeQuestSlot, int refreshSlot, int filSlot, int page, String filter, org.bukkit.configuration.file.FileConfiguration bountyConfig, ItemStack currentItem, SolarElo plugin) {
        if (slot == prevSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            if (page > 1) {
                EloGui.openBounty(plugin, player, page - 1, filter);
            } else {
                EloGui.openMainMenu(plugin, player);
            }
        } else if (slot == nextSlot) {
            if (currentItem != null && currentItem.getType() != Material.AIR && !currentItem.getType().name().endsWith("_GLASS_PANE")) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openBounty(plugin, player, page + 1, filter);
            }
        } else if (slot == activeQuestSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openActiveQuest(plugin, player);
        } else if (slot == refreshSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openBounty(plugin, player, page, filter);
        } else if (slot == filSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            java.util.List<String> options = bountyConfig.getStringList("filter.options");
            if (options == null || options.isEmpty()) {
                options = java.util.Arrays.asList("HIGH_TO_LOW", "LOW_TO_HIGH");
            }
            int idx = options.indexOf(filter.toUpperCase());
            int nextIdx = (idx + 1) % options.size();
            String nextFilter = options.get(nextIdx);
            EloGui.openBounty(plugin, player, 1, nextFilter);
        }
    }

    private static void handleBountyHeadClick(Player player, ItemStack currentItem, java.util.UUID activeTarget, long remaining, org.bukkit.configuration.file.FileConfiguration bountyConfig, SolarElo plugin) {
        org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) currentItem.getItemMeta();
        if (skullMeta == null) return;

        org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "target_uuid");
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
            if (target.getUniqueId().equals(player.getUniqueId())) {
                plugin.getEffectManager().playGuiSound(player, "error");
                return;
            }

            if (activeTarget != null) {
                plugin.getEffectManager().playGuiSound(player, "error");
                player.sendMessage(EloGui.colorize("#ff3c3c[Nhiệm Vụ] Bạn đã nhận một hợp đồng. Hãy hoàn thành hoặc hủy nó trước."));
                return;
            }

            if (remaining > 0) {
                plugin.getEffectManager().playGuiSound(player, "error");
                player.sendMessage(EloGui.colorize("#ff3c3c[Nhiệm Vụ] Bạn đang trong thời gian chờ. Vui lòng đợi thêm " + remaining + " giây."));
                return;
            }

            String clickSound = bountyConfig.getString("target-player-head.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, clickSound);
            EloGui.openBountyConfirm(plugin, player, target.getUniqueId(), target.getName() != null ? target.getName() : "Unknown");
        }
    }

    public static void handleActiveQuestClick(org.bukkit.event.inventory.InventoryClickEvent event, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration activeConfig = plugin.getGuiConfigManager().getActiveQuestConfig();
        int cancelSlot = EloGui.getSlotFromLayout(activeConfig, 'c', activeConfig.getInt("cancel-item.slot", 15));
        int backSlot = EloGui.getSlotFromLayout(activeConfig, 'a', activeConfig.getInt("back-button.slot", 22));

        if (slot == cancelSlot) {
            UUID activeTarget = plugin.getEloManager().getActiveBountyTarget(player.getUniqueId());
            if (activeTarget != null) {
                plugin.getEloManager().clearActiveBountyTarget(player.getUniqueId());

                int cancelCooldown = plugin.getBountyConfig().getInt("bounty-quest.cancel-cooldown-seconds", 300);
                long cancelCooldownEnd = System.currentTimeMillis() + (cancelCooldown * 1000L);
                plugin.getEloManager().setBountyCooldown(player.getUniqueId(), cancelCooldownEnd);

                String cancelSound = activeConfig.getString("cancel-item.confirm_sound", "error");
                plugin.getEffectManager().playGuiSound(player, cancelSound);
                player.sendMessage(EloGui.colorize("#ff3c3c[Nhiệm Vụ] Bạn đã hủy hợp đồng săn thưởng. Phạt chờ " + cancelCooldown + " giây."));
                player.closeInventory();
            } else {
                plugin.getEffectManager().playGuiSound(player, "error");
            }
        } else if (slot == backSlot) {
            String clickSound = activeConfig.getString("back-button.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, clickSound);
            EloGui.openBounty(plugin, player);
        }
    }

    public static void handleBountyConfirmClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.BountyConfirmHolder confirmHolder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration confirmConfig = plugin.getGuiConfigManager().getConfirmationConfig();

        java.util.List<Integer> confirmSlots = EloGui.getSlotsFromLayout(confirmConfig, 'c');
        if (confirmSlots.isEmpty()) {
            confirmSlots = confirmConfig.getIntegerList("confirm-item.slots");
            if (confirmSlots.isEmpty() && confirmConfig.contains("confirm-item.slot")) {
                confirmSlots = java.util.Collections.singletonList(confirmConfig.getInt("confirm-item.slot"));
            }
        }

        java.util.List<Integer> cancelSlots = EloGui.getSlotsFromLayout(confirmConfig, 'a');
        if (cancelSlots.isEmpty()) {
            cancelSlots = confirmConfig.getIntegerList("cancel-item.slots");
            if (cancelSlots.isEmpty() && confirmConfig.contains("cancel-item.slot")) {
                cancelSlots = java.util.Collections.singletonList(confirmConfig.getInt("cancel-item.slot"));
            }
        }

        if (confirmSlots.contains(slot)) {
            PlayerData selfData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
            if (selfData != null && selfData.isLocked()) {
                plugin.getEffectManager().playGuiSound(player, "error");
                String msg = plugin.getMessageManager().get("bounty-locked-error", "&cElo của bạn đang bị khóa, không thể sử dụng tính năng săn thưởng!");
                player.sendMessage(EloGui.colorize(msg));
                player.closeInventory();
                return;
            }
            UUID targetUuid = confirmHolder.getTargetUuid();
            Player target = org.bukkit.Bukkit.getPlayer(targetUuid);

            if (target == null || !target.isOnline()) {
                plugin.getEffectManager().playGuiSound(player, "error");
                player.sendMessage(EloGui.colorize("#ff3c3c[Nhiệm Vụ] Mục tiêu không còn online hoặc không hợp lệ."));
                EloGui.openBounty(plugin, player);
                return;
            }

            plugin.getEloManager().setActiveBountyTarget(player.getUniqueId(), targetUuid);
            String confirmSound = confirmConfig.getString("confirm-item.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, confirmSound);

            player.sendMessage(EloGui.colorize("#00ff3c[Nhiệm Vụ] Bạn đã nhận nhiệm vụ săn thưởng! Mục tiêu: " + target.getName()));

            PlayerData targetData = plugin.getEloManager().getData(target.getUniqueId(), target.getName());
            if (targetData != null && targetData.isSettingWelcomeEffect()) {
                String targetMsg = plugin.getMessageManager().get("bounty-targeted",
                    "#ffaa00[Nhiệm Vụ] Một thợ săn đã nhận khế ước truy nã bạn!");
                dev.solar.solarelo.managers.MessageManager.sendMessage(target, targetMsg);
            }

            player.closeInventory();
        } else if (cancelSlots.contains(slot)) {
            String cancelSound = confirmConfig.getString("cancel-item.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, cancelSound);
            EloGui.openBounty(plugin, player);
        }
    }
}
