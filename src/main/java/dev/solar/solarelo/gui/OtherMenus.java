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

            plugin.runForEntity(player, () -> {
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

            plugin.runForEntity(player, () -> {
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
