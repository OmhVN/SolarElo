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

public class OtherMenus {

    public static void openMainMenu(SolarElo plugin, Player player) {
        boolean isBlocked = EloGui.checkIpBlockedRedirect(plugin, player, true);
        plugin.runAsync(() -> {
            org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getMainConfig();
            List<String> disposition = config.getStringList("gui-disposition");
            String title = EloGui.colorize(config.getString("title", "#ffaa00SolarElo Menu"));
            int tempRows = config.getInt("rows", 3);
            if (disposition != null && !disposition.isEmpty()) {
                tempRows = disposition.size();
            }
            if (tempRows < 1 || tempRows > 6) tempRows = 3;
            int rows = tempRows;

            plugin.runSync(() -> {
                EloGui.MainMenuHolder holder = new EloGui.MainMenuHolder();
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                boolean fillerEnabled = config.getBoolean("filler.enabled", true);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(config.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
                    ItemStack pane = new ItemStack(paneMat);
                    ItemMeta paneMeta = pane.getItemMeta();
                    if (paneMeta != null) {
                        paneMeta.setDisplayName(" ");
                        pane.setItemMeta(paneMeta);
                    }
                    for (int i = 0; i < rows * 9; i++) {
                        inv.setItem(i, pane);
                    }
                }

                if (isBlocked) {
                    String blockPath = "ip-blocked-item";
                    int blockSlot = EloGui.getSlotFromLayout(config, 'i', config.getInt(blockPath + ".slot", 13));
                    if (blockSlot >= 0 && blockSlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString(blockPath + ".material"), Material.RED_BANNER);
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString(blockPath + ".name", "#ff3c3c⚠ FEATURE LOCKED ⚠")));
                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList(blockPath + ".lore")) {
                                lore.add(EloGui.colorize(l));
                            }
                            meta.setLore(lore);
                            int cmd = config.getInt(blockPath + ".customModelData", -1);
                            if (cmd != -1) meta.setCustomModelData(cmd);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(blockSlot, item);
                    }
                } else {
                    int bountySlot = EloGui.getSlotFromLayout(config, 'b', config.getInt("bounty-item.slot", 11));
                    boolean bountyEnabled = plugin.getBountyConfig().getBoolean("bounty.enabled", true)
                            && plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)
                            && config.getBoolean("bounty-item.enabled", true);
                    if (bountyEnabled && bountySlot >= 0 && bountySlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString("bounty-item.material"), Material.DIAMOND_SWORD);
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString("bounty-item.name", "#ff3c3c⚔ ʙᴏᴜɴᴛỷ ⚔")));
                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList("bounty-item.lore")) {
                                lore.add(EloGui.colorize(l));
                            }
                            meta.setLore(lore);
                            int cmd = config.getInt("bounty-item.customModelData", -1);
                            if (cmd != -1) meta.setCustomModelData(cmd);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(bountySlot, item);
                    }

                    int leaderSlot = EloGui.getSlotFromLayout(config, 'l', config.getInt("leaderboard-item.slot", 13));
                    boolean leaderEnabled = plugin.getGuiConfigManager().getLeaderboardConfig().getBoolean("enabled", true)
                            && config.getBoolean("leaderboard-item.enabled", true);
                    if (leaderEnabled && leaderSlot >= 0 && leaderSlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString("leaderboard-item.material"), Material.BLUE_BANNER);
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString("leaderboard-item.name", "#00BFFF★ ʟᴇᴀᴅᴇʀʙᴏᴀʀᴅ ★")));
                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList("leaderboard-item.lore")) {
                                lore.add(EloGui.colorize(l));
                            }
                            meta.setLore(lore);
                            int cmd = config.getInt("leaderboard-item.customModelData", -1);
                            if (cmd != -1) meta.setCustomModelData(cmd);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(leaderSlot, item);
                    }

                    int rewardSlot = EloGui.getSlotFromLayout(config, 'r', config.getInt("rewards-item.slot", 15));
                    boolean rewardsEnabled = plugin.getGuiConfigManager().getRewardsConfig().getBoolean("enabled", true)
                            && config.getBoolean("rewards-item.enabled", true);
                    if (rewardsEnabled && rewardSlot >= 0 && rewardSlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString("rewards-item.material"), Material.CHEST);
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString("rewards-item.name", "#00ff3c✪ ʀᴇᴡᴀʀᴅs ✪")));
                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList("rewards-item.lore")) {
                                lore.add(EloGui.colorize(l));
                            }
                            meta.setLore(lore);
                            int cmd = config.getInt("rewards-item.customModelData", -1);
                            if (cmd != -1) meta.setCustomModelData(cmd);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(rewardSlot, item);
                    }

                    int settingsSlot = EloGui.getSlotFromLayout(config, 's', config.getInt("settings-item.slot", 22));
                    boolean settingsEnabled = plugin.getGuiConfigManager().getSettingsConfig().getBoolean("enabled", true)
                            && config.getBoolean("settings-item.enabled", true);
                    if (settingsEnabled && settingsSlot >= 0 && settingsSlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString("settings-item.material"), Material.COMPARATOR);
                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString("settings-item.name", "#ffaa00⚙ Settings ⚙")));
                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList("settings-item.lore")) {
                                lore.add(EloGui.colorize(l));
                            }
                            meta.setLore(lore);
                            int cmd = config.getInt("settings-item.customModelData", -1);
                            if (cmd != -1) meta.setCustomModelData(cmd);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(settingsSlot, item);
                    }
                }

                player.openInventory(inv);
            });
        });
    }

    public static void openSettings(SolarElo plugin, Player player) {
        if (!plugin.getGuiConfigManager().getSettingsConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-settings", "&#ff3c3cTính năng Cài đặt hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;

        plugin.runAsync(() -> {
            PlayerData data = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
            final PlayerData finalData = data;

            org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getSettingsConfig();
            List<String> disposition = config.getStringList("gui-disposition");
            String title = EloGui.colorize(config.getString("title", "sᴇᴛᴛɪɴɢs"));
            int tempRows = config.getInt("rows", 3);
            if (disposition != null && !disposition.isEmpty()) {
                tempRows = disposition.size();
            }
            if (tempRows < 1 || tempRows > 6) tempRows = 3;
            int rows = tempRows;

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                EloGui.SettingsHolder holder = new EloGui.SettingsHolder();
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                boolean fillerEnabled = config.getBoolean("filler.enabled", true);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(config.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
                    ItemStack pane = new ItemStack(paneMat);
                    ItemMeta paneMeta = pane.getItemMeta();
                    if (paneMeta != null) {
                        paneMeta.setDisplayName(" ");
                        pane.setItemMeta(paneMeta);
                    }
                    for (int i = 0; i < rows * 9; i++) {
                        inv.setItem(i, pane);
                    }
                }

                String[] toggleKeys = { "chat-notification", "title-notification", "bounty-notification" };
                for (String key : toggleKeys) {
                    String path = "items." + key;
                    if (!config.contains(path)) continue;

                    char symbol;
                    if (key.equals("chat-notification")) symbol = 'c';
                    else if (key.equals("title-notification")) symbol = 't';
                    else symbol = 'b';
                    int slot = EloGui.getSlotFromLayout(config, symbol, config.getInt(path + ".slot", -1));
                    if (slot < 0 || slot >= rows * 9) continue;

                    boolean state = true;
                    if (key.equals("chat-notification")) state = finalData.isSettingChat();
                    else if (key.equals("title-notification")) state = finalData.isSettingTitle();
                    else if (key.equals("bounty-notification")) state = finalData.isSettingWelcomeEffect();

                    Material mat = EloGui.getMaterial(config.getString(path + ".material"), Material.PAPER);
                    ItemStack item = new ItemStack(mat);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(EloGui.colorize(config.getString(path + ".name", key)));
                        List<String> lore = new ArrayList<>();
                        List<String> rawLore = config.getStringList(state ? (path + ".lore_on") : (path + ".lore_off"));
                        for (String l : rawLore) {
                            lore.add(EloGui.colorize(l));
                        }
                        meta.setLore(lore);
                        int cmd = config.getInt(path + ".customModelData", -1);
                        if (cmd != -1) meta.setCustomModelData(cmd);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(slot, item);
                }

                int backSlot = EloGui.getSlotFromLayout(config, 'a', config.getInt("back-button.slot", 22));
                if (backSlot >= 0 && backSlot < rows * 9) {
                    Material mat = EloGui.getMaterial(config.getString("back-button.material"), Material.RED_STAINED_GLASS_PANE);
                    ItemStack backItem = new ItemStack(mat);
                    ItemMeta backMeta = backItem.getItemMeta();
                    if (backMeta != null) {
                        backMeta.setDisplayName(EloGui.colorize(config.getString("back-button.name", "#ff3c3cʙᴀᴄᴋ")));
                        List<String> backLore = new ArrayList<>();
                        for (String l : config.getStringList("back-button.lore")) {
                            backLore.add(EloGui.colorize(l));
                        }
                        backMeta.setLore(backLore);
                        int cmd = config.getInt("back-button.customModelData", -1);
                        if (cmd != -1) backMeta.setCustomModelData(cmd);
                        backItem.setItemMeta(backMeta);
                    }
                    inv.setItem(backSlot, backItem);
                }

                player.openInventory(inv);
            });
        });
    }

    public static void openBountyConfirm(SolarElo plugin, Player player, UUID targetUuid, String targetName) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        org.bukkit.configuration.file.FileConfiguration bountyConfig = plugin.getGuiConfigManager().getBountyConfig();
        org.bukkit.configuration.file.FileConfiguration confirmConfig = plugin.getGuiConfigManager().getConfirmationConfig();
        List<String> disposition = confirmConfig.getStringList("gui-disposition");
        int tempRows = confirmConfig.getInt("rows", 3);
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 3;
        final int rows = tempRows;

        EloGui.BountyConfirmHolder holder = new EloGui.BountyConfirmHolder(targetUuid, targetName);
        String titleTemplate = confirmConfig.getString("title", "#ff3c3cᴄᴏɴꜰɪʀᴍ ʙᴏᴜɴᴛʏ: {target}");
        String title = EloGui.colorize(titleTemplate.replace("{target}", targetName));
        Inventory inv = EloGui.createInventory(holder, rows * 9, title);
        holder.setInventory(inv);

        boolean fillerEnabled = confirmConfig.getBoolean("filler.enabled", true);
        if (fillerEnabled) {
            Material paneMat = EloGui.getMaterial(confirmConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
            ItemStack pane = new ItemStack(paneMat);
            ItemMeta paneMeta = pane.getItemMeta();
            if (paneMeta != null) {
                paneMeta.setDisplayName(" ");
                pane.setItemMeta(paneMeta);
            }
            for (int i = 0; i < rows * 9; i++) {
                inv.setItem(i, pane);
            }
        }

        int targetSlot = EloGui.getSlotFromLayout(confirmConfig, 't', confirmConfig.getInt("target-slot", 13));
        if (targetSlot >= 0 && targetSlot < rows * 9) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                SkinsRestorerHook.applySkin(skullMeta, targetUuid, targetName);
                skullMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("target-player-head.name", "#ff3c3cᴛᴀʀɢᴇᴛ #ffffff{player}").replace("{player}", targetName)));

                int rewardElo = plugin.getBountyConfig().getInt("bounty-quest.reward-elo", 20);
                PlayerData tData = plugin.getEloManager().getCachedData(targetUuid);
                int elo = tData != null ? tData.getElo() : 1000;
                String rankKey = plugin.getRankManager().getRank(elo);
                String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

                List<String> lore = new ArrayList<>();
                for (String l : bountyConfig.getStringList("target-player-head.lore")) {
                    lore.add(EloGui.colorize(l.replace("{elo}", EloGui.formatNumber(elo))
                            .replace("{rank}", rankDisplay)
                            .replace("{reward_elo}", EloGui.formatNumber(rewardElo))));
                }
                skullMeta.setLore(lore);
                head.setItemMeta(skullMeta);
            }
            inv.setItem(targetSlot, head);
        }

        String confirmPath = "confirm-item";
        Material confirmMat = EloGui.getMaterial(confirmConfig.getString(confirmPath + ".material"), Material.LIME_STAINED_GLASS_PANE);
        ItemStack confirmItem = new ItemStack(confirmMat);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName(EloGui.colorize(confirmConfig.getString(confirmPath + ".name", "#00ff3cᴄᴏɴꜰɪʀᴍ")));
            List<String> lore = new ArrayList<>();
            for (String l : confirmConfig.getStringList(confirmPath + ".lore")) {
                lore.add(EloGui.colorize(l.replace("{target}", targetName)));
            }
            confirmMeta.setLore(lore);
            int cmd = confirmConfig.getInt(confirmPath + ".customModelData", -1);
            if (cmd != -1) confirmMeta.setCustomModelData(cmd);
            confirmItem.setItemMeta(confirmMeta);
        }
        List<Integer> confirmSlots = EloGui.getSlotsFromLayout(confirmConfig, 'c');
        if (confirmSlots.isEmpty()) {
            confirmSlots = confirmConfig.getIntegerList(confirmPath + ".slots");
            if (confirmSlots.isEmpty() && confirmConfig.contains(confirmPath + ".slot")) {
                confirmSlots = java.util.Collections.singletonList(confirmConfig.getInt(confirmPath + ".slot"));
            }
        }
        for (int slot : confirmSlots) {
            if (slot >= 0 && slot < rows * 9) {
                inv.setItem(slot, confirmItem);
            }
        }

        String cancelPath = "cancel-item";
        Material cancelMat = EloGui.getMaterial(confirmConfig.getString(cancelPath + ".material"), Material.RED_STAINED_GLASS_PANE);
        ItemStack cancelItem = new ItemStack(cancelMat);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(EloGui.colorize(confirmConfig.getString(cancelPath + ".name", "#ff3c3cɢᴏ ʙᴀᴄᴋ")));
            List<String> lore = new ArrayList<>();
            for (String l : confirmConfig.getStringList(cancelPath + ".lore")) {
                lore.add(EloGui.colorize(l.replace("{target}", targetName)));
            }
            cancelMeta.setLore(lore);
            int cmd = confirmConfig.getInt(cancelPath + ".customModelData", -1);
            if (cmd != -1) cancelMeta.setCustomModelData(cmd);
            cancelItem.setItemMeta(cancelMeta);
        }
        List<Integer> cancelSlots = EloGui.getSlotsFromLayout(confirmConfig, 'a');
        if (cancelSlots.isEmpty()) {
            cancelSlots = confirmConfig.getIntegerList(cancelPath + ".slots");
            if (cancelSlots.isEmpty() && confirmConfig.contains(cancelPath + ".slot")) {
                cancelSlots = java.util.Collections.singletonList(confirmConfig.getInt(cancelPath + ".slot"));
            }
        }
        for (int slot : cancelSlots) {
            if (slot >= 0 && slot < rows * 9) {
                inv.setItem(slot, cancelItem);
            }
        }

        player.openInventory(inv);
    }

    public static void openActiveQuest(SolarElo plugin, Player player) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getActiveQuestConfig();
        List<String> disposition = config.getStringList("gui-disposition");
        int tempRows = config.getInt("rows", 3);
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 3;
        final int rows = tempRows;

        UUID activeTargetUuid = plugin.getEloManager().getActiveBountyTarget(player.getUniqueId());

        plugin.runAsync(() -> {
            org.bukkit.OfflinePlayer targetPlayer = activeTargetUuid != null ? Bukkit.getOfflinePlayer(activeTargetUuid) : null;
            String targetName = (targetPlayer != null && targetPlayer.getName() != null) ? targetPlayer.getName() : "Không Có";

            PlayerData tData = null;
            if (activeTargetUuid != null) {
                tData = plugin.getEloManager().getCachedData(activeTargetUuid);
                if (tData == null) {
                    tData = plugin.getEloManager().getData(activeTargetUuid, targetName);
                }
            }
            final PlayerData finalTData = tData;

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                EloGui.ActiveQuestHolder holder = new EloGui.ActiveQuestHolder();
                String titleTemplate = config.getString("title", "#ff3c3cᴀᴄᴛɪᴠᴇ ᴄᴏɴᴛʀᴀᴄᴛ");
                long activeEnd = activeTargetUuid != null ? plugin.getEloManager().getActiveBountyEndTime(player.getUniqueId()) : 0;
                long activeRemaining = activeTargetUuid != null ? (activeEnd - System.currentTimeMillis()) / 1000 : 0;
                String activeRemainingStr = activeTargetUuid != null ? EloGui.formatTimeRemaining(activeRemaining) : "0s";

                String title = EloGui.colorize(titleTemplate
                        .replace("{target}", targetName)
                        .replace("{time_remaining}", activeRemainingStr));

                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                boolean fillerEnabled = config.getBoolean("filler.enabled", false);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(config.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
                    ItemStack pane = new ItemStack(paneMat);
                    ItemMeta paneMeta = pane.getItemMeta();
                    if (paneMeta != null) {
                        paneMeta.setDisplayName(" ");
                        pane.setItemMeta(paneMeta);
                    }
                    for (int i = 0; i < rows * 9; i++) {
                        inv.setItem(i, pane);
                    }
                }

                int rewardElo = plugin.getBountyConfig().getInt("bounty-quest.reward-elo", 20);

                if (activeTargetUuid != null) {
                    int activeSlot = EloGui.getSlotFromLayout(config, 'q', config.getInt("active-item.slot", 11));
                    if (activeSlot >= 0 && activeSlot < rows * 9 && finalTData != null) {
                        ItemStack activeItem = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) activeItem.getItemMeta();
                        if (skullMeta != null) {
                            SkinsRestorerHook.applySkin(skullMeta, activeTargetUuid, targetName);

                            String rankKey = plugin.getRankManager().getRank(finalTData.getElo());
                            String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

                            skullMeta.setDisplayName(EloGui.colorize(config.getString("active-item.name", "#ff3c3cᴛᴀʀɢᴇᴛ #ffffff{target}")
                                    .replace("{target}", targetName)));

                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList("active-item.lore")) {
                                lore.add(EloGui.colorize(l.replace("{target}", targetName)
                                        .replace("{elo}", EloGui.formatNumber(finalTData.getElo()))
                                        .replace("{rank}", rankDisplay)
                                        .replace("{reward_elo}", EloGui.formatNumber(rewardElo))
                                        .replace("{time_remaining}", activeRemainingStr)));
                            }
                            skullMeta.setLore(lore);
                            activeItem.setItemMeta(skullMeta);
                        }
                        inv.setItem(activeSlot, activeItem);
                    }

                    int cancelSlot = EloGui.getSlotFromLayout(config, 'c', config.getInt("cancel-item.slot", 15));
                    if (cancelSlot >= 0 && cancelSlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString("cancel-item.material"), Material.BARRIER);
                        ItemStack cancelItem = new ItemStack(mat);
                        ItemMeta meta = cancelItem.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString("cancel-item.name", "#ff3c3cᴄᴀɴᴄᴇʟ ᴄᴏɴᴛʀᴀᴄᴛ")));
                            List<String> lore = new ArrayList<>();
                            int cancelCooldown = plugin.getBountyConfig().getInt("bounty-quest.cancel-cooldown-seconds", 300);
                            for (String l : config.getStringList("cancel-item.lore")) {
                                lore.add(EloGui.colorize(l.replace("{cooldown}", String.valueOf(cancelCooldown))));
                            }
                            meta.setLore(lore);
                            cancelItem.setItemMeta(meta);
                        }
                        inv.setItem(cancelSlot, cancelItem);
                    }
                } else {
                    int noQuestSlot = EloGui.getSlotFromLayout(config, 'n', config.getInt("no-quest-item.slot", 13));
                    if (noQuestSlot >= 0 && noQuestSlot < rows * 9) {
                        Material mat = EloGui.getMaterial(config.getString("no-quest-item.material"), Material.BARRIER);
                        ItemStack noQuestItem = new ItemStack(mat);
                        ItemMeta meta = noQuestItem.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(EloGui.colorize(config.getString("no-quest-item.name", "#ff3c3cɴᴏ ᴀᴄᴛɪᴠᴇ ᴄᴏɴᴛʀᴀᴄᴛ")));
                            List<String> lore = new ArrayList<>();
                            for (String l : config.getStringList("no-quest-item.lore")) {
                                lore.add(EloGui.colorize(l));
                            }
                            meta.setLore(lore);
                            noQuestItem.setItemMeta(meta);
                        }
                        inv.setItem(noQuestSlot, noQuestItem);
                    }
                }

                int backSlot = EloGui.getSlotFromLayout(config, 'a', config.getInt("back-button.slot", 22));
                if (backSlot >= 0 && backSlot < rows * 9) {
                    Material mat = EloGui.getMaterial(config.getString("back-button.material"), Material.ARROW);
                    ItemStack backItem = new ItemStack(mat);
                    ItemMeta meta = backItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(EloGui.colorize(config.getString("back-button.name", "#00BFFFʙᴀᴄᴋ")));
                        List<String> lore = new ArrayList<>();
                        for (String l : config.getStringList("back-button.lore")) {
                            lore.add(EloGui.colorize(l));
                        }
                        meta.setLore(lore);
                        int cmd = config.getInt("back-button.customModelData", -1);
                        if (cmd != -1) {
                            meta.setCustomModelData(cmd);
                        }
                        backItem.setItemMeta(meta);
                    }
                    inv.setItem(backSlot, backItem);
                }

                player.openInventory(inv);
            });
        });
    }

    public static void handleMainMenuClick(org.bukkit.event.inventory.InventoryClickEvent event, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getMainConfig();
        int bountySlot = EloGui.getSlotFromLayout(config, 'b', config.getInt("bounty-item.slot", 11));
        int leaderSlot = EloGui.getSlotFromLayout(config, 'l', config.getInt("leaderboard-item.slot", 13));
        int rewardSlot = EloGui.getSlotFromLayout(config, 'r', config.getInt("rewards-item.slot", 15));
        int settingsSlot = EloGui.getSlotFromLayout(config, 's', config.getInt("settings-item.slot", 22));

        if (slot == bountySlot && bountySlot != -1) {
            boolean bountyEnabled = plugin.getBountyConfig().getBoolean("bounty.enabled", true)
                    && plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)
                    && config.getBoolean("bounty-item.enabled", true);
            if (bountyEnabled) {
                PlayerData pData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
                if (pData != null && pData.isLocked()) {
                    plugin.getEffectManager().playGuiSound(player, "error");
                    String msg = plugin.getMessageManager().get("bounty-locked-error", "&cElo của bạn đang bị khóa, không thể sử dụng tính năng săn thưởng!");
                    player.sendMessage(EloGui.colorize(msg));
                    return;
                }
                String soundKey = config.getString("bounty-item.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openBounty(plugin, player);
            } else {
                plugin.getEffectManager().playGuiSound(player, "error");
                plugin.getMessageManager().send(player, "gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            }
        } else if (slot == leaderSlot && leaderSlot != -1) {
            boolean leaderEnabled = plugin.getGuiConfigManager().getLeaderboardConfig().getBoolean("enabled", true)
                    && config.getBoolean("leaderboard-item.enabled", true);
            if (leaderEnabled) {
                String soundKey = config.getString("leaderboard-item.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openLeaderboard(plugin, player, 1, "HIGH_TO_LOW");
            } else {
                plugin.getEffectManager().playGuiSound(player, "error");
                plugin.getMessageManager().send(player, "gui-disabled-leaderboard", "&#ff3c3cTính năng Bảng xếp hạng hiện đang bị tắt.");
            }
        } else if (slot == rewardSlot && rewardSlot != -1) {
            boolean rewardsEnabled = plugin.getGuiConfigManager().getRewardsConfig().getBoolean("enabled", true)
                    && config.getBoolean("rewards-item.enabled", true);
            if (rewardsEnabled) {
                String soundKey = config.getString("rewards-item.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openRankRewards(plugin, player, -1, "HIGH_TO_LOW");
            } else {
                plugin.getEffectManager().playGuiSound(player, "error");
                plugin.getMessageManager().send(player, "gui-disabled-rewards", "&#ff3c3cTính năng Phần thưởng hiện đang bị tắt.");
            }
        } else if (slot == settingsSlot && settingsSlot != -1) {
            boolean settingsEnabled = plugin.getGuiConfigManager().getSettingsConfig().getBoolean("enabled", true)
                    && config.getBoolean("settings-item.enabled", true);
            if (settingsEnabled) {
                String soundKey = config.getString("settings-item.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openSettings(plugin, player);
            } else {
                plugin.getEffectManager().playGuiSound(player, "error");
                plugin.getMessageManager().send(player, "gui-disabled-settings", "&#ff3c3cTính năng Cài đặt hiện đang bị tắt.");
            }
        }
    }

    public static void handleSettingsClick(org.bukkit.event.inventory.InventoryClickEvent event, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getSettingsConfig();
        int backSlot = EloGui.getSlotFromLayout(config, 'a', config.getInt("back-button.slot", 22));

        if (slot == backSlot) {
            String soundKey = config.getString("back-button.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);
            EloGui.openMainMenu(plugin, player);
            return;
        }

        int chatSlot = EloGui.getSlotFromLayout(config, 'c', config.getInt("items.chat-notification.slot", 11));
        int titleSlot = EloGui.getSlotFromLayout(config, 't', config.getInt("items.title-notification.slot", 15));
        int bountySlot = EloGui.getSlotFromLayout(config, 'b', config.getInt("items.bounty-notification.slot", 13));

        PlayerData data = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
        if (data != null) {
            if (slot == chatSlot) {
                data.setSettingChat(!data.isSettingChat());
                plugin.runAsync(() -> plugin.getDatabaseManager().savePlayer(data));
                String soundKey = config.getString("items.chat-notification.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openSettings(plugin, player);
            } else if (slot == titleSlot) {
                data.setSettingTitle(!data.isSettingTitle());
                plugin.runAsync(() -> plugin.getDatabaseManager().savePlayer(data));
                String soundKey = config.getString("items.title-notification.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openSettings(plugin, player);
            } else if (slot == bountySlot) {
                data.setSettingWelcomeEffect(!data.isSettingWelcomeEffect());
                plugin.runAsync(() -> plugin.getDatabaseManager().savePlayer(data));
                String soundKey = config.getString("items.bounty-notification.confirm_sound", "click");
                plugin.getEffectManager().playGuiSound(player, soundKey);
                EloGui.openSettings(plugin, player);
            }
        }

    }
}
