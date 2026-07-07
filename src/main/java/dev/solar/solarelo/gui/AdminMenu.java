package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.api.model.EloHistoryEntry;
import dev.solar.solarelo.api.model.KillHistoryEntry;
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

public class AdminMenu {

    public static void openEloAdmin(SolarElo plugin, Player player) {
        openEloAdmin(plugin, player, 1);
    }

    public static void openEloAdmin(SolarElo plugin, Player player, int page) {
        if (!player.hasPermission("solarelo.admin")) return;
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }

        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        List<String> disposition = adminConfig.getStringList("admin-list.gui-disposition");
        int tempRows = 6;
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 6;
        final int rows = tempRows;

        int tempLimit = 45;
        if (disposition != null && !disposition.isEmpty()) {
            int count = 0;
            for (String row : disposition) {
                for (int c = 0; c < row.length() && c < 9; c++) {
                    if (row.charAt(c) == 'x') count++;
                }
            }
            tempLimit = count;
        }
        final int limit = tempLimit;
        final int offset = (page - 1) * limit;

        plugin.runAsync(() -> {
            List<PlayerData> dbPlayers = plugin.getDatabaseManager().getTopPlayers(limit, offset, true);
            int totalPlayers = plugin.getDatabaseManager().getTotalPlayers();
            boolean hasNextPage = totalPlayers > (page * limit);

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                String titleTemplate = adminConfig.getString("admin-list.title", "ᴇʟᴏ ᴀᴅᴍɪɴ - {page}");
                String title = EloGui.colorize(titleTemplate.replace("{page}", String.valueOf(page)));
                EloGui.EloAdminHolder holder = new EloGui.EloAdminHolder(page);
                Inventory inv = EloGui.createInventory(holder, 54, title);
                holder.setInventory(inv);

                boolean fillerEnabled = adminConfig.getBoolean("filler.enabled", true);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(adminConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
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
                        for (int i = 45; i < 54; i++) {
                            inv.setItem(i, pane);
                        }
                    }
                }

                int slot = 0;
                for (PlayerData pData : dbPlayers) {
                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                    if (skullMeta != null) {
                        SkinsRestorerHook.applySkin(skullMeta, pData.getUuid(), pData.getName());
                        org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "target_uuid");
                        skullMeta.getPersistentDataContainer().set(uuidKey, org.bukkit.persistence.PersistentDataType.STRING, pData.getUuid().toString());

                        skullMeta.setDisplayName(EloGui.colorize("&e" + pData.getName()));
                        List<String> lore = new ArrayList<>();
                        lore.add(EloGui.colorize("&7Elo: &e" + EloGui.formatNumber(pData.getElo())));
                        lore.add(EloGui.colorize("&7Kills: &a" + pData.getKills() + " &7| Deaths: &c" + pData.getDeaths()));
                        lore.add(EloGui.colorize("&7Streak: &c" + pData.getCurrentStreak() + " &7(Best: &e" + pData.getBestStreak() + "&7)"));
                        lore.add("");
                        lore.add(EloGui.colorize("&aClick to manage player!"));
                        skullMeta.setLore(lore);
                        head.setItemMeta(skullMeta);
                    }
                    inv.setItem(slot++, head);
                }

                if (page > 1) {
                    ItemStack prevItem = EloGui.loadConfigItem(adminConfig, "back", "ARROW", "#00BFFFʙᴀᴄᴋ", Arrays.asList("&fClick to go to the previous page"), -1);
                    inv.setItem(adminConfig.getInt("back.slot", 45), prevItem);
                }

                ItemStack refreshItem = EloGui.loadConfigItem(adminConfig, "refresh", "FEATHER", "#00ff3cʀᴇꜰʀᴇsʜ", Arrays.asList("&fClick to refresh the list"), -1);
                inv.setItem(adminConfig.getInt("refresh.slot", 49), refreshItem);

                if (hasNextPage) {
                    ItemStack nextItem = EloGui.loadConfigItem(adminConfig, "next", "ARROW", "#00BFFFɴᴇxᴛ", Arrays.asList("&fClick to go to the next page"), -1);
                    inv.setItem(adminConfig.getInt("next.slot", 53), nextItem);
                }

                ItemStack searchItem = EloGui.loadConfigItem(adminConfig, "search", "COMPASS", "#ffaa00s\u1d07\u1d00\u0280\u1d04\u029c", Arrays.asList("&fClick to search a player by name"), -1);
                inv.setItem(adminConfig.getInt("search.slot", 47), searchItem);

                player.openInventory(inv);
            });
        });
    }

    public static void openEloAdminDetail(SolarElo plugin, Player player, UUID targetUuid, String targetName) {
        if (!player.hasPermission("solarelo.admin")) return;
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }

        plugin.runAsync(() -> {
            PlayerData tData = plugin.getEloManager().getCachedData(targetUuid);
            if (tData == null) {
                tData = plugin.getDatabaseManager().loadPlayer(targetUuid, targetName);
            }
            final PlayerData finalTData = tData;

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
                String titleTemplate = adminConfig.getString("admin-detail.title", "ᴀᴅᴍɪɴ ᴅᴇᴛᴀɪʟs - {player}");
                String title = EloGui.colorize(titleTemplate.replace("{player}", targetName));
                EloGui.EloAdminDetailHolder holder = new EloGui.EloAdminDetailHolder(targetUuid, targetName);
                Inventory inv = EloGui.createInventory(holder, 45, title);
                holder.setInventory(inv);

                boolean fillerEnabled = adminConfig.getBoolean("filler.enabled", true);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(adminConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
                    ItemStack pane = new ItemStack(paneMat);
                    ItemMeta paneMeta = pane.getItemMeta();
                    if (paneMeta != null) {
                        paneMeta.setDisplayName(" ");
                        pane.setItemMeta(paneMeta);
                    }
                    for (int i = 0; i < 45; i++) {
                        inv.setItem(i, pane);
                    }
                }

                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                if (skullMeta != null) {
                    SkinsRestorerHook.applySkin(skullMeta, targetUuid, targetName);
                    skullMeta.setDisplayName(EloGui.colorize("&e&l" + targetName));
                    List<String> lore = new ArrayList<>();
                    lore.add(EloGui.colorize("&7Elo: &e" + EloGui.formatNumber(finalTData.getElo())));
                    lore.add(EloGui.colorize("&7Kills: &a" + finalTData.getKills() + " &7| Deaths: &c" + finalTData.getDeaths()));
                    lore.add(EloGui.colorize("&7Streak: &c" + finalTData.getCurrentStreak() + " &7(Best: &e" + finalTData.getBestStreak() + "&7)"));
                    lore.add(EloGui.colorize("&7Last Known IP: &f" + (finalTData.getLastIp() != null ? finalTData.getLastIp() : "None")));
                    lore.add("");
                    lore.add(EloGui.colorize("&aClick to Check Alts (Same IP)!"));
                    skullMeta.setLore(lore);
                    head.setItemMeta(skullMeta);
                }
                inv.setItem(13, head);

                ItemStack addBlock = EloGui.loadConfigItem(adminConfig, "add-elo", "EMERALD", "#00ff3cᴀᴅᴅ ᴇʟᴏ", Arrays.asList("&fClick to add ELO to this player"), -1);
                inv.setItem(adminConfig.getInt("add-elo.slot", 28), addBlock);

                ItemStack setBlock = EloGui.loadConfigItem(adminConfig, "set-elo", "NAME_TAG", "#ffaa00sᴇᴛ ᴇʟᴏ", Arrays.asList("&fClick to set player's ELO"), -1);
                inv.setItem(adminConfig.getInt("set-elo.slot", 29), setBlock);

                ItemStack removeBlock = EloGui.loadConfigItem(adminConfig, "remove-elo", "REDSTONE", "#ff3c3c\u0280\u1d07\u1d0d\u1d0f\u1d20\u1d07 ᴇʟᴏ", Arrays.asList("&fClick to deduct ELO from player"), -1);
                inv.setItem(adminConfig.getInt("remove-elo.slot", 30), removeBlock);

                ItemStack backItem = EloGui.loadConfigItem(adminConfig, "back-to-list", "ARROW", "#aaaaaa\u029c\u1d00\u1d04\u1d0b", Arrays.asList("&fClick to return to player list"), -1);
                inv.setItem(adminConfig.getInt("back-to-list.slot", 31), backItem);

                ItemStack historyBook = EloGui.loadConfigItem(adminConfig, "elo-history", "BOOK", "#00BFFF\u1d07\u029f\u1d0f ʜɪsᴛᴏʀʏ", Arrays.asList("&fClick to view ELO change history"), -1);
                inv.setItem(adminConfig.getInt("elo-history.slot", 32), historyBook);

                ItemStack pvpSword = EloGui.loadConfigItem(adminConfig, "pvp-history", "DIAMOND_SWORD", "#00BFFF\u1d18\u1d20\u1d18 ʜɪsᴛᴏʀʏ", Arrays.asList("&fClick to view PvP logs"), -1);
                inv.setItem(adminConfig.getInt("pvp-history.slot", 33), pvpSword);

                ItemStack resetBlock = EloGui.loadConfigItem(adminConfig, "reset-stats", "GUNPOWDER", "#ff3c3c\u0280\u1d07s\u1d07\u1d1b sᴛᴀᴛs", Arrays.asList("&fClick to reset ELO and stats to default"), -1);
                inv.setItem(adminConfig.getInt("reset-stats.slot", 34), resetBlock);

                ItemStack searchItem = EloGui.loadConfigItem(adminConfig, "search", "COMPASS", "#ffaa00s\u1d07\u1d00\u0280\u1d04\u029c", Arrays.asList("&fClick to search a player by name"), -1);
                inv.setItem(adminConfig.getInt("search.slot", 47), searchItem);

                player.openInventory(inv);
            });
        });
    }

    public static void openEloHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page) {
        openEloHistory(plugin, player, targetUuid, targetName, page, "ALL");
    }

    public static void openEloHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page, String filter) {
        if (!player.hasPermission("solarelo.admin")) return;
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }

        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        List<String> disposition = adminConfig.getStringList("elo-history.gui-disposition");
        int tempRows = 6;
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 6;
        final int rows = tempRows;

        int tempLimit = 45;
        if (disposition != null && !disposition.isEmpty()) {
            int count = 0;
            for (String row : disposition) {
                for (int c = 0; c < row.length() && c < 9; c++) {
                    if (row.charAt(c) == 'x') count++;
                }
            }
            tempLimit = count;
        }
        final int limit = tempLimit;
        final int offset = (page - 1) * limit;

        plugin.runAsync(() -> {
            List<EloHistoryEntry> history = plugin.getDatabaseManager().getEloHistory(targetUuid);
            long threeDaysAgo = System.currentTimeMillis() - (3L * 24L * 60L * 60L * 1000L);
            List<EloHistoryEntry> filtered = new ArrayList<>();
            for (EloHistoryEntry entry : history) {
                if (entry.getTimestamp() < threeDaysAgo) continue;
                if (filter.equalsIgnoreCase("ADD")) {
                    if (entry.getChangeAmount() >= 0) filtered.add(entry);
                } else if (filter.equalsIgnoreCase("REMOVE")) {
                    if (entry.getChangeAmount() < 0) filtered.add(entry);
                } else {
                    filtered.add(entry);
                }
            }

            int total = filtered.size();
            List<EloHistoryEntry> paginated = new ArrayList<>();
            for (int i = offset; i < Math.min(total, offset + limit); i++) {
                paginated.add(filtered.get(i));
            }
            boolean hasNextPage = total > (page * limit);

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                String titleTemplate = adminConfig.getString("elo-history.title", "ᴇʟᴏ ʜɪsᴛᴏʀʏ - {player}");
                String title = EloGui.colorize(titleTemplate.replace("{player}", targetName).replace("{page}", String.valueOf(page)));
                EloGui.EloHistoryHolder holder = new EloGui.EloHistoryHolder(targetUuid, targetName, page, filter);
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                boolean fillerEnabled = adminConfig.getBoolean("filler.enabled", true);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(adminConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
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
                        for (int i = 45; i < 54; i++) {
                            inv.setItem(i, pane);
                        }
                    }
                }

                List<Integer> lbSlots = null;
                if (disposition != null && !disposition.isEmpty()) {
                    lbSlots = EloGui.getSlotsFromLayout(adminConfig, "elo-history.gui-disposition", 'x');
                }

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                int slot = 0;
                int idx = 0;
                for (EloHistoryEntry entry : paginated) {
                    ItemStack item = new ItemStack(Material.PAPER);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        String prefixStr = entry.getChangeAmount() >= 0 ? "&a+" : "&c";
                        meta.setDisplayName(EloGui.colorize(prefixStr + entry.getChangeAmount() + " Elo"));
                        List<String> lore = new ArrayList<>();
                        lore.add(EloGui.colorize("&7Lý do: &f" + entry.getReason()));
                        lore.add(EloGui.colorize("&7Thời gian: &7" + sdf.format(new java.util.Date(entry.getTimestamp()))));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    if (lbSlots != null) {
                        if (idx < lbSlots.size()) {
                            inv.setItem(lbSlots.get(idx++), item);
                        }
                    } else {
                        inv.setItem(slot++, item);
                    }
                }

                if (page > 1) {
                    ItemStack prevItem = EloGui.loadConfigItem(adminConfig, "back", "ARROW", "#00BFFFʙᴀᴄᴋ", Arrays.asList("&fClick to go to the previous page"), -1);
                    inv.setItem(adminConfig.getInt("back.slot", 45), prevItem);
                }

                ItemStack filterItem = EloGui.loadConfigItem(adminConfig, "filter", "HOPPER", "#00BFFFғɪʟᴛᴇʀ", Arrays.asList("&fClick to toggle filter"), -1);
                ItemMeta filterMeta = filterItem.getItemMeta();
                if (filterMeta != null) {
                    List<String> lore = filterMeta.getLore();
                    if (lore == null) lore = new ArrayList<>();
                    lore.add("");
                    String filterStr = filter.equalsIgnoreCase("ALL") ? "&eTất cả" : filter.equalsIgnoreCase("ADD") ? "&aCộng Elo" : "&cTrừ Elo";
                    lore.add(EloGui.colorize("&7Bộ lọc hiện tại: " + filterStr));
                    lore.add(EloGui.colorize("&aClick để thay đổi bộ lọc"));
                    filterMeta.setLore(lore);
                    filterItem.setItemMeta(filterMeta);
                }
                inv.setItem(adminConfig.getInt("filter.slot", 49), filterItem);

                if (hasNextPage) {
                    ItemStack nextItem = EloGui.loadConfigItem(adminConfig, "next", "ARROW", "#00BFFFɴᴇxᴛ", Arrays.asList("&fClick to go to the next page"), -1);
                    inv.setItem(adminConfig.getInt("next.slot", 53), nextItem);
                }

                ItemStack searchItem = EloGui.loadConfigItem(adminConfig, "search", "COMPASS", "#ffaa00s\u1d07\u1d00\u0280\u1d04\u029c", Arrays.asList("&fClick to search a player by name"), -1);
                inv.setItem(adminConfig.getInt("search.slot", 47), searchItem);

                player.openInventory(inv);
            });
        });
    }

    public static void openKillHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page) {
        openKillHistory(plugin, player, targetUuid, targetName, page, "ALL");
    }

    public static void openKillHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page, String filter) {
        if (!player.hasPermission("solarelo.admin")) return;
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }

        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        List<String> disposition = adminConfig.getStringList("kill-history.gui-disposition");
        int tempRows = 6;
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 6;
        final int rows = tempRows;

        int tempLimit = 45;
        if (disposition != null && !disposition.isEmpty()) {
            int count = 0;
            for (String row : disposition) {
                for (int c = 0; c < row.length() && c < 9; c++) {
                    if (row.charAt(c) == 'x') count++;
                }
            }
            tempLimit = count;
        }
        final int limit = tempLimit;
        final int offset = (page - 1) * limit;

        plugin.runAsync(() -> {
            List<KillHistoryEntry> history = plugin.getDatabaseManager().getKillHistory(targetUuid);
            long threeDaysAgo = System.currentTimeMillis() - (3L * 24L * 60L * 60L * 1000L);
            List<KillHistoryEntry> filtered = new ArrayList<>();
            for (KillHistoryEntry entry : history) {
                if (entry.getTimestamp() < threeDaysAgo) continue;
                if (filter.equalsIgnoreCase("KILLS")) {
                    if (entry.getKillerUuid().equals(targetUuid)) filtered.add(entry);
                } else if (filter.equalsIgnoreCase("DEATHS")) {
                    if (!entry.getKillerUuid().equals(targetUuid)) filtered.add(entry);
                } else {
                    filtered.add(entry);
                }
            }

            int total = filtered.size();
            List<KillHistoryEntry> paginated = new ArrayList<>();
            for (int i = offset; i < Math.min(total, offset + limit); i++) {
                paginated.add(filtered.get(i));
            }
            boolean hasNextPage = total > (page * limit);

            List<String> displayNames = new ArrayList<>();
            for (KillHistoryEntry entry : paginated) {
                if (entry.getKillerUuid().equals(targetUuid)) {
                    String victimName = plugin.getDatabaseManager().getPlayerName(entry.getVictimUuid());
                    displayNames.add("&a⚔ Kill: &f" + victimName);
                } else {
                    String killerName = plugin.getDatabaseManager().getPlayerName(entry.getKillerUuid());
                    displayNames.add("&c☠ Death: &f" + killerName);
                }
            }

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                String titleTemplate = adminConfig.getString("kill-history.title", "ᴘᴠᴘ ʜɪsᴛᴏʀʏ - {player}");
                String title = EloGui.colorize(titleTemplate.replace("{player}", targetName).replace("{page}", String.valueOf(page)));
                EloGui.KillHistoryHolder holder = new EloGui.KillHistoryHolder(targetUuid, targetName, page, filter);
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                boolean fillerEnabled = adminConfig.getBoolean("filler.enabled", true);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(adminConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
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
                        for (int i = 45; i < 54; i++) {
                            inv.setItem(i, pane);
                        }
                    }
                }

                List<Integer> lbSlots = null;
                if (disposition != null && !disposition.isEmpty()) {
                    lbSlots = EloGui.getSlotsFromLayout(adminConfig, "kill-history.gui-disposition", 'x');
                }

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                int slot = 0;
                int idx = 0;
                for (int i = 0; i < paginated.size(); i++) {
                    KillHistoryEntry entry = paginated.get(i);
                    String name = displayNames.get(i);
                    Material mat = entry.getKillerUuid().equals(targetUuid) ? Material.DIAMOND_SWORD : Material.BONE;
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(EloGui.colorize(name));
                        List<String> lore = new ArrayList<>();
                        lore.add(EloGui.colorize("&7Thời gian: &e" + sdf.format(new java.util.Date(entry.getTimestamp()))));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    if (lbSlots != null) {
                        if (idx < lbSlots.size()) {
                            inv.setItem(lbSlots.get(idx++), item);
                        }
                    } else {
                        inv.setItem(slot++, item);
                    }
                }

                int prevSlot = EloGui.getSlotFromLayout(adminConfig, "kill-history.gui-disposition", 'b', adminConfig.getInt("back.slot", 45));
                if (page > 1 && prevSlot >= 0 && prevSlot < rows * 9) {
                    ItemStack prevItem = EloGui.loadConfigItem(adminConfig, "back", "ARROW", "#00BFFF\u029c\u1d00\u1d04\u1d0b", Arrays.asList("&fClick to go to the previous page"), -1);
                    inv.setItem(prevSlot, prevItem);
                }

                int filterSlot = EloGui.getSlotFromLayout(adminConfig, "kill-history.gui-disposition", 'f', adminConfig.getInt("filter.slot", 49));
                if (filterSlot >= 0 && filterSlot < rows * 9) {
                    ItemStack filterItem = EloGui.loadConfigItem(adminConfig, "filter", "HOPPER", "#00BFFF\u0252\u026a\u029f\u1d1b\u1d07\u0280", Arrays.asList("&fClick to toggle filter"), -1);
                    ItemMeta filterMeta = filterItem.getItemMeta();
                    if (filterMeta != null) {
                        List<String> lore = filterMeta.getLore();
                        if (lore == null) lore = new ArrayList<>();
                        lore.add("");
                        String filterStr = filter.equalsIgnoreCase("ALL") ? "&eTất cả" : filter.equalsIgnoreCase("KILLS") ? "&aMạng giết" : "&cMạng chết";
                        lore.add(EloGui.colorize("&7Bộ lọc hiện tại: " + filterStr));
                        lore.add(EloGui.colorize("&aClick để thay đổi bộ lọc"));
                        filterMeta.setLore(lore);
                        filterItem.setItemMeta(filterMeta);
                    }
                    inv.setItem(filterSlot, filterItem);
                }

                int nextSlot = EloGui.getSlotFromLayout(adminConfig, "kill-history.gui-disposition", 'n', adminConfig.getInt("next.slot", 53));
                if (hasNextPage && nextSlot >= 0 && nextSlot < rows * 9) {
                    ItemStack nextItem = EloGui.loadConfigItem(adminConfig, "next", "ARROW", "#00BFFF\u0274\u1d07x\u1d1b", Arrays.asList("&fClick to go to the next page"), -1);
                    inv.setItem(nextSlot, nextItem);
                }

                ItemStack searchItem = EloGui.loadConfigItem(adminConfig, "search", "COMPASS", "#ffaa00s\u1d07\u1d00\u0280\u1d04\u029c", Arrays.asList("&fClick to search a player by name"), -1);
                inv.setItem(adminConfig.getInt("search.slot", 47), searchItem);

                player.openInventory(inv);
            });
        });
    }

    public static void handleEloAdminClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.EloAdminHolder adminHolder, Player player, int slot, SolarElo plugin) {
        int page = adminHolder.getPage();
        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        int backSlot    = EloGui.getSlotFromLayout(adminConfig, "admin-list.gui-disposition", 'b', adminConfig.getInt("back.slot", 45));
        int nextSlot    = EloGui.getSlotFromLayout(adminConfig, "admin-list.gui-disposition", 'n', adminConfig.getInt("next.slot", 53));
        int refreshSlot = EloGui.getSlotFromLayout(adminConfig, "admin-list.gui-disposition", 'r', adminConfig.getInt("refresh.slot", 49));
        int searchSlot  = EloGui.getSlotFromLayout(adminConfig, "admin-list.gui-disposition", 'q', adminConfig.getInt("search.slot", 47));

        if (slot == backSlot) {
            if (page > 1) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openEloAdmin(plugin, player, page - 1);
            }
        } else if (slot == searchSlot) {

            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            if (dev.solar.solarelo.gui.ClientCompatibility.supportsDialog(player)) {
                dev.solar.solarelo.gui.DialogInputHelper.showSearchDialog(plugin, player);
            } else if (dev.solar.solarelo.gui.FloodgateFormHelper.isBedrockPlayer(player)) {
                dev.solar.solarelo.gui.FloodgateFormHelper.showSearchForm(plugin, player);
            } else {
                dev.solar.solarelo.gui.FloodgateFormHelper.triggerChatSearch(plugin, player);
            }
        } else if (slot == refreshSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openEloAdmin(plugin, player, page);
        } else if (slot == nextSlot) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && currentItem.getType() != Material.AIR && !currentItem.getType().name().endsWith("_GLASS_PANE")) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openEloAdmin(plugin, player, page + 1);
            }
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) event.getCurrentItem().getItemMeta();
            if (skullMeta != null) {
                org.bukkit.NamespacedKey uuidKey = new org.bukkit.NamespacedKey(plugin, "target_uuid");
                String str = skullMeta.getPersistentDataContainer().get(uuidKey, org.bukkit.persistence.PersistentDataType.STRING);
                UUID targetUuid = str != null ? UUID.fromString(str) : null;
                if (targetUuid != null) {
                    plugin.getEffectManager().playGuiSound(player, "click");
                    String targetName = org.bukkit.ChatColor.stripColor(skullMeta.getDisplayName());
                    plugin.runAsync(() -> {
                        PlayerData tData = plugin.getEloManager().getCachedData(targetUuid);
                        if (tData == null) {
                            tData = plugin.getDatabaseManager().loadPlayer(targetUuid, targetName);
                        }
                        if (tData != null) {
                            String ip = tData.getLastIp();
                            if (ip != null && !ip.isEmpty()) {
                                java.util.List<PlayerData> alts = plugin.getDatabaseManager().getAlts(ip);
                                String header = plugin.getMessageManager().get("alts-list-header",
                                    "&8&m                                                    \n&#ffaa00&lDANH SÁCH TÀI KHOẢN CÙNG IP\n&7IP Address: &#ffffff{ip}\n")
                                    .replace("{ip}", ip);
                                player.sendMessage(EloGui.colorize(header));

                                String itemFormat = plugin.getMessageManager().get("alts-list-item",
                                    "  &8▪ &#ffffff{player} &7- &#ffaa00{elo} ELO &7({status}&7)");
                                String onlineStatus = plugin.getMessageManager().get("alts-status-online", "&#00ff3c● &7Online");
                                String offlineStatus = plugin.getMessageManager().get("alts-status-offline", "&7● &7Offline");

                                for (PlayerData alt : alts) {
                                    String statusStr = org.bukkit.Bukkit.getPlayer(alt.getUuid()) != null ? onlineStatus : offlineStatus;
                                    String itemMsg = itemFormat
                                        .replace("{player}", alt.getName())
                                        .replace("{elo}", String.valueOf(alt.getElo()))
                                        .replace("{status}", statusStr);
                                    player.sendMessage(EloGui.colorize(itemMsg));
                                }

                                String footer = plugin.getMessageManager().get("alts-list-footer", "&8&m                                                    ");
                                if (!footer.isEmpty()) {
                                    player.sendMessage(EloGui.colorize(footer));
                                }
                            }
                        }
                    });

                    EloGui.openEloAdminDetail(plugin, player, targetUuid, targetName);
                }
            }
        }
    }

    public static void handleEloAdminDetailClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.EloAdminDetailHolder detailHolder, Player player, int slot, SolarElo plugin) {
        UUID targetUuid = detailHolder.getTargetUuid();
        String targetName = detailHolder.getTargetName();

        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        int addSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 'a', adminConfig.getInt("add-elo.slot", 28));
        int setSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 's', adminConfig.getInt("set-elo.slot", 29));
        int removeSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 'r', adminConfig.getInt("remove-elo.slot", 30));
        int backSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 'b', adminConfig.getInt("back-to-list.slot", 31));
        int eloHistSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 'e', adminConfig.getInt("elo-history.slot", 32));
        int pvpHistSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 'p', adminConfig.getInt("pvp-history.slot", 33));
        int resetSlot = EloGui.getSlotFromLayout(adminConfig, "admin-detail.gui-disposition", 'x', adminConfig.getInt("reset-stats.slot", 34));

        if (slot == 13) {
            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            checkAltsForAdmin(player, targetUuid, targetName, plugin);
        } else if (slot == addSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            if (dev.solar.solarelo.gui.ClientCompatibility.supportsDialog(player)) {
                dev.solar.solarelo.gui.DialogInputHelper.showEloInputDialog(plugin, player, targetUuid, targetName, "add");
            } else {
                player.sendMessage(EloGui.colorize("&#ffaa00ᴇʟᴏ ᴀᴅᴍɪɴ &8» &fNhập số ELO muốn &a&lCỘNG &fcho &#ffffff" + targetName + " &f(Gõ &#ff3c3ccancel&f để hủy):"));
                registerChatPrompt(player, targetUuid, targetName, "add");
            }
        } else if (slot == setSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            if (dev.solar.solarelo.gui.ClientCompatibility.supportsDialog(player)) {
                dev.solar.solarelo.gui.DialogInputHelper.showEloInputDialog(plugin, player, targetUuid, targetName, "set");
            } else {
                player.sendMessage(EloGui.colorize("&#ffaa00ᴇʟᴏ ᴀᴅᴍɪɴ &8» &fNhập số ELO muốn &6&lĐẶT &fcho &#ffffff" + targetName + " &f(Gõ &#ff3c3ccancel&f để hủy):"));
                registerChatPrompt(player, targetUuid, targetName, "set");
            }
        } else if (slot == removeSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            if (dev.solar.solarelo.gui.ClientCompatibility.supportsDialog(player)) {
                dev.solar.solarelo.gui.DialogInputHelper.showEloInputDialog(plugin, player, targetUuid, targetName, "remove");
            } else {
                player.sendMessage(EloGui.colorize("&#ffaa00ᴇʟᴏ ᴀᴅᴍɪɴ &8» &fNhập số ELO muốn &c&lTRỪ &fcủa &#ffffff" + targetName + " &f(Gõ &#ff3c3ccancel&f để hủy):"));
                registerChatPrompt(player, targetUuid, targetName, "remove");
            }
        } else if (slot == backSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openEloAdmin(plugin, player, 1);
        } else if (slot == eloHistSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openEloHistory(plugin, player, targetUuid, targetName, 1);
        } else if (slot == pvpHistSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openKillHistory(plugin, player, targetUuid, targetName, 1);
        } else if (slot == resetSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            plugin.getEloManager().resetElo(targetUuid, targetName);
            player.sendMessage(EloGui.colorize("&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã reset ELO & Stats cho &#ffffff" + targetName));
        }
    }

    private static void checkAltsForAdmin(Player player, UUID targetUuid, String targetName, SolarElo plugin) {
        plugin.runAsync(() -> {
            PlayerData tData = plugin.getEloManager().getCachedData(targetUuid);
            if (tData == null) {
                tData = plugin.getDatabaseManager().loadPlayer(targetUuid, targetName);
            }
            String ip = tData.getLastIp();
            if (ip == null || ip.isEmpty()) {
                String noIpMsg = plugin.getMessageManager().get("alts-no-ip",
                    "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy dữ liệu IP của &#ffffff{player} &cđể kiểm tra alts.")
                    .replace("{player}", targetName);
                player.sendMessage(EloGui.colorize(noIpMsg));
                return;
            }

            java.util.List<PlayerData> alts = plugin.getDatabaseManager().getAlts(ip);
            String header = plugin.getMessageManager().get("alts-list-header-admin",
                "&#ffaa00&m                                                    \n&#ffaa00ᴀʟᴛ ᴀｃｃｏｕｎｔｓ &8» &fDanh sách tài khoản cùng IP\n&7IP Address: &#ffffff{ip}\n")
                .replace("{ip}", ip);
            player.sendMessage(EloGui.colorize(header));

            String itemFormat = plugin.getMessageManager().get("alts-list-item",
                "  &8▪ &#ffffff{player} &7- &#ffaa00{elo} ELO &7({status}&7)");
            String onlineStatus = plugin.getMessageManager().get("alts-status-online", "&#00ff3c● &7Online");
            String offlineStatus = plugin.getMessageManager().get("alts-status-offline", "&7● &7Offline");

            for (PlayerData alt : alts) {
                String statusStr = org.bukkit.Bukkit.getPlayer(alt.getUuid()) != null ? onlineStatus : offlineStatus;
                String itemMsg = itemFormat
                    .replace("{player}", alt.getName())
                    .replace("{elo}", String.valueOf(alt.getElo()))
                    .replace("{status}", statusStr);
                player.sendMessage(EloGui.colorize(itemMsg));
            }

            String footer = plugin.getMessageManager().get("alts-list-footer-admin", "&#ffaa00&m                                                    ");
            if (!footer.isEmpty()) {
                player.sendMessage(EloGui.colorize(footer));
            }
        });
    }

    private static void registerChatPrompt(Player admin, UUID targetUuid, String targetName, String action) {
        dev.solar.solarelo.listeners.GuiListener.chatPrompts.put(admin.getUniqueId(), new dev.solar.solarelo.listeners.GuiListener.ChatPromptData(targetUuid, targetName, action));
    }

    public static void handleEloHistoryClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.EloHistoryHolder historyHolder, Player player, int slot, SolarElo plugin) {
        int page = historyHolder.getPage();
        UUID targetUuid = historyHolder.getTargetUuid();
        String targetName = historyHolder.getTargetName();
        String filter = historyHolder.getFilter();

        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        int backSlot = EloGui.getSlotFromLayout(adminConfig, "elo-history.gui-disposition", 'b', adminConfig.getInt("back.slot", 45));
        int nextSlot = EloGui.getSlotFromLayout(adminConfig, "elo-history.gui-disposition", 'n', adminConfig.getInt("next.slot", 53));
        int filterSlot = EloGui.getSlotFromLayout(adminConfig, "elo-history.gui-disposition", 'f', adminConfig.getInt("filter.slot", 49));

        if (slot == backSlot) {
            if (page > 1) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openEloHistory(plugin, player, targetUuid, targetName, page - 1, filter);
            }
        } else if (slot == filterSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            String nextFilter = switch (filter) {
                case "ALL" -> "ADD";
                case "ADD" -> "REMOVE";
                default -> "ALL";
            };
            EloGui.openEloHistory(plugin, player, targetUuid, targetName, 1, nextFilter);
        } else if (slot == nextSlot) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && currentItem.getType() != Material.AIR && !currentItem.getType().name().endsWith("_GLASS_PANE")) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openEloHistory(plugin, player, targetUuid, targetName, page + 1, filter);
            }
        }
    }

    public static void handleKillHistoryClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.KillHistoryHolder killHistoryHolder, Player player, int slot, SolarElo plugin) {
        int page = killHistoryHolder.getPage();
        UUID targetUuid = killHistoryHolder.getTargetUuid();
        String targetName = killHistoryHolder.getTargetName();
        String filter = killHistoryHolder.getFilter();

        org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
        int backSlot = EloGui.getSlotFromLayout(adminConfig, "elo-history.gui-disposition", 'b', adminConfig.getInt("back.slot", 45));
        int nextSlot = EloGui.getSlotFromLayout(adminConfig, "elo-history.gui-disposition", 'n', adminConfig.getInt("next.slot", 53));
        int filterSlot = EloGui.getSlotFromLayout(adminConfig, "elo-history.gui-disposition", 'f', adminConfig.getInt("filter.slot", 49));

        if (slot == backSlot) {
            if (page > 1) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openKillHistory(plugin, player, targetUuid, targetName, page - 1, filter);
            }
        } else if (slot == filterSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            String nextFilter = switch (filter) {
                case "ALL" -> "KILLS";
                case "KILLS" -> "DEATHS";
                default -> "ALL";
            };
            EloGui.openKillHistory(plugin, player, targetUuid, targetName, 1, nextFilter);
        } else if (slot == nextSlot) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && currentItem.getType() != Material.AIR && !currentItem.getType().name().endsWith("_GLASS_PANE")) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openKillHistory(plugin, player, targetUuid, targetName, page + 1, filter);
            }
        }
    }
}
