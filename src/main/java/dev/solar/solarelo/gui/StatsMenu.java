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
import java.util.List;

public class StatsMenu {

    public static void open(SolarElo plugin, Player player, String targetPlayerName, int returnPage, String returnFilter) {
        if (!plugin.getGuiConfigManager().getStatsConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-stats", "&#ff3c3cTính năng Xem thống kê bằng giao diện hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        org.bukkit.configuration.file.FileConfiguration statsConfig = plugin.getGuiConfigManager().getStatsConfig();
        List<String> disposition = statsConfig.getStringList("gui-disposition");
        int tempRows = statsConfig.getInt("rows", 3);
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 3;
        final int rows = tempRows;

        plugin.runAsync(() -> {
            org.bukkit.OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
            PlayerData pData = plugin.getDatabaseManager().loadPlayer(targetPlayer.getUniqueId(), targetPlayerName);
            if (pData == null) {
                pData = new PlayerData(targetPlayer.getUniqueId(), targetPlayerName, plugin.getConfig().getInt("elo.default-elo", 1000), 0, 0);
            }
            final PlayerData finalPData = pData;

            plugin.runSync(() -> {
                if (!player.isOnline()) return;

                EloGui.StatsHolder holder = new EloGui.StatsHolder(targetPlayerName, returnPage, returnFilter);
                String titleTemplate = statsConfig.getString("title", "#555555sᴛᴀᴛs - {player}");
                String title = EloGui.colorize(titleTemplate.replace("{player}", targetPlayerName));
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                boolean fillerEnabled = statsConfig.getBoolean("filler.enabled", false);
                if (fillerEnabled) {
                    Material paneMat = EloGui.getMaterial(statsConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
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

                String rankKey = plugin.getRankManager().getRank(finalPData.getElo());
                String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);
                String rankPrefix = plugin.getRankManager().getRankPrefix(rankKey);

                String[] statsKeys = { "player-head", "rank", "elo", "kills", "deaths", "kd", "streak", "best_streak" };
                for (String key : statsKeys) {
                    String path = "items." + key;
                    if (!statsConfig.contains(path)) continue;

                    char symbol;
                    switch (key) {
                        case "player-head": symbol = 'h'; break;
                        case "rank": symbol = 'r'; break;
                        case "elo": symbol = 'e'; break;
                        case "kills": symbol = 'k'; break;
                        case "deaths": symbol = 'd'; break;
                        case "kd": symbol = 'q'; break;
                        case "streak": symbol = 's'; break;
                        case "best_streak": symbol = 'b'; break;
                        default: symbol = ' '; break;
                    }
                    int slot = EloGui.getSlotFromLayout(statsConfig, symbol, statsConfig.getInt(path + ".slot", -1));
                    if (slot < 0 || slot >= rows * 9) continue;

                    Material mat = EloGui.getMaterial(statsConfig.getString(path + ".material"), Material.BARRIER);
                    ItemStack item;
                    if (mat == Material.PLAYER_HEAD) {
                        item = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                        if (skullMeta != null) {
                            SkinsRestorerHook.applySkin(skullMeta, targetPlayer.getUniqueId(), targetPlayerName);
                            item.setItemMeta(skullMeta);
                        }
                    } else {
                        item = new ItemStack(mat);
                    }

                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        String name = statsConfig.getString(path + ".name", "");
                        name = name.replace("{player}", targetPlayerName)
                                  .replace("{rank}", rankDisplay)
                                  .replace("{prefix}", rankPrefix)
                                  .replace("{elo}", EloGui.formatNumber(finalPData.getElo()))
                                  .replace("{kills}", EloGui.formatNumber(finalPData.getKills()))
                                  .replace("{deaths}", EloGui.formatNumber(finalPData.getDeaths()))
                                  .replace("{kd}", EloGui.formatNumber(finalPData.getKDRatio()))
                                  .replace("{streak}", EloGui.formatNumber(finalPData.getCurrentStreak()))
                                  .replace("{best_streak}", EloGui.formatNumber(finalPData.getBestStreak()));
                        meta.setDisplayName(EloGui.colorize(name));

                        List<String> lore = new ArrayList<>();
                        for (String l : statsConfig.getStringList(path + ".lore")) {
                            lore.add(EloGui.colorize(l.replace("{player}", targetPlayerName)
                                  .replace("{rank}", rankDisplay)
                                  .replace("{prefix}", rankPrefix)
                                  .replace("{elo}", EloGui.formatNumber(finalPData.getElo()))
                                  .replace("{kills}", EloGui.formatNumber(finalPData.getKills()))
                                  .replace("{deaths}", EloGui.formatNumber(finalPData.getDeaths()))
                                  .replace("{kd}", EloGui.formatNumber(finalPData.getKDRatio()))
                                  .replace("{streak}", EloGui.formatNumber(finalPData.getCurrentStreak()))
                                  .replace("{best_streak}", EloGui.formatNumber(finalPData.getBestStreak()))
                            ));
                        }
                        meta.setLore(lore);

                        int cmd = statsConfig.getInt(path + ".customModelData", -1);
                        if (cmd != -1) {
                            meta.setCustomModelData(cmd);
                        }
                        item.setItemMeta(meta);
                    }
                    inv.setItem(slot, item);
                }

                int backSlot = EloGui.getSlotFromLayout(statsConfig, 'a', statsConfig.getInt("back-button.slot", 22));
                if (backSlot >= 0 && backSlot < rows * 9) {
                    Material mat = EloGui.getMaterial(statsConfig.getString("back-button.material"), Material.RED_STAINED_GLASS_PANE);
                    ItemStack backItem = new ItemStack(mat);
                    ItemMeta backMeta = backItem.getItemMeta();
                    if (backMeta != null) {
                        backMeta.setDisplayName(EloGui.colorize(statsConfig.getString("back-button.name", "#ff3c3c\u029c\u1d0f\u1d1c\u0274\u1d1b\u028f")));
                        List<String> backLore = new ArrayList<>();
                        for (String l : statsConfig.getStringList("back-button.lore")) {
                            backLore.add(EloGui.colorize(l));
                        }
                        backMeta.setLore(backLore);
                        int cmd = statsConfig.getInt("back-button.customModelData", -1);
                        if (cmd != -1) {
                            backMeta.setCustomModelData(cmd);
                        }
                        backItem.setItemMeta(backMeta);
                    }
                    inv.setItem(backSlot, backItem);
                }

                player.openInventory(inv);
            });
        });
    }

    public static void handleInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.StatsHolder statsHolder, Player player, int slot, SolarElo plugin) {
        int backSlot = EloGui.getSlotFromLayout(plugin.getGuiConfigManager().getStatsConfig(), 'a', plugin.getGuiConfigManager().getStatsConfig().getInt("back-button.slot", 22));
        if (slot == backSlot) {
            String soundKey = plugin.getGuiConfigManager().getStatsConfig().getString("back-button.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);
            if (statsHolder.getReturnPage() == -1) {
                EloGui.openMainMenu(plugin, player);
            } else {
                EloGui.openLeaderboard(plugin, player, statsHolder.getReturnPage(), statsHolder.getReturnFilter());
            }
        }
    }

}
