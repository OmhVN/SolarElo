package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class RankRewardsMenu {

    public static void open(SolarElo plugin, Player player, int returnPage, String returnFilter) {
        if (!plugin.getGuiConfigManager().getRewardsConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-rewards", "&#ff3c3cTính năng Phần thưởng hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        org.bukkit.configuration.file.FileConfiguration rewConfig = plugin.getGuiConfigManager().getRewardsConfig();
        List<String> disposition = rewConfig.getStringList("gui-disposition");
        int tempRows = rewConfig.getInt("rows", 5);
        if (disposition != null && !disposition.isEmpty()) {
            tempRows = disposition.size();
        }
        if (tempRows < 1 || tempRows > 6) tempRows = 5;
        final int rows = tempRows;

        EloGui.RankRewardsHolder holder = new EloGui.RankRewardsHolder(returnPage, returnFilter);
        String title = EloGui.colorize(rewConfig.getString("title", "#555555\u0280\u1d00\u0274\u1d0b \u0280\u1d07\u1d21\u1d00\u0280\u1d07s"));
        Inventory inv = EloGui.createInventory(holder, rows * 9, title);
        holder.setInventory(inv);

        boolean fillerEnabled = rewConfig.getBoolean("filler.enabled", false);
        if (fillerEnabled) {
            Material paneMat = EloGui.getMaterial(rewConfig.getString("filler.material"), Material.BLACK_STAINED_GLASS_PANE);
            ItemStack bg = new ItemStack(paneMat);
            ItemMeta bgMeta = bg.getItemMeta();
            if (bgMeta != null) {
                bgMeta.setDisplayName(" ");
                bg.setItemMeta(bgMeta);
            }
            for (int i = 0; i < rows * 9; i++) {
                inv.setItem(i, bg);
            }
        }

        LinkedHashMap<String, Integer> ranks = plugin.getRankManager().getRankMinElo();
        int playerElo = 0;
        PlayerData pData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
        if (pData != null) {
            playerElo = pData.getElo();
        }

        List<String> rankKeys = new ArrayList<>(ranks.keySet());
        int count = rankKeys.size();

        List<Integer> slotsToUse = new ArrayList<>();
        if (disposition != null && !disposition.isEmpty()) {
            slotsToUse = EloGui.getSlotsFromLayout(rewConfig, 'x');
        } else {
            if (rows == 5) {
                if (count <= 7) {
                    int rowStart = 18 + (9 - count) / 2;
                    for (int j = 0; j < count; j++) {
                        slotsToUse.add(rowStart + j);
                    }
                } else if (count <= 14) {
                    int row2Count = count / 2;
                    int row1Count = count - row2Count;

                    int r1Start = 10 + (7 - row1Count) / 2;
                    for (int j = 0; j < row1Count; j++) {
                        slotsToUse.add(r1Start + j);
                    }
                    int r2Start = 19 + (7 - row2Count) / 2;
                    for (int j = 0; j < row2Count; j++) {
                        slotsToUse.add(r2Start + j);
                    }
                } else {
                    int itemsPerRow = (count + 2) / 3;
                    int row1Count = itemsPerRow;
                    int row2Count = itemsPerRow;
                    int row3Count = count - row1Count - row2Count;

                    int r1Start = 10 + (7 - row1Count) / 2;
                    for (int j = 0; j < row1Count; j++) {
                        slotsToUse.add(r1Start + j);
                    }
                    int r2Start = 19 + (7 - row2Count) / 2;
                    for (int j = 0; j < row2Count; j++) {
                        slotsToUse.add(r2Start + j);
                    }
                    int r3Start = 28 + (7 - row3Count) / 2;
                    for (int j = 0; j < row3Count; j++) {
                        slotsToUse.add(r3Start + j);
                    }
                }
            } else {
                int startSlot = 9;
                if (count < 9) {
                    startSlot = 9 + (9 - count) / 2;
                }
                for (int j = 0; j < count; j++) {
                    slotsToUse.add(startSlot + j);
                }
            }
        }

        String itemDisplayNameTemplate = rewConfig.getString("rank-item.name", "{rank}");
        List<String> itemLoreTemplate = rewConfig.getStringList("rank-item.lore");
        if (itemLoreTemplate.isEmpty()) {
            itemLoreTemplate = Arrays.asList("#aaaaaaRequired Elo: #ffaa00{min_elo}", "#aaaaaaPrefix: &r{prefix}", "", "#ffaa00Breakthrough rewards:", "{rewards}", "", "{status}");
        }
        String rewardLineFormat = rewConfig.getString("rank-item.reward-line-format", " &a• &f{command}");
        String noRewardsFormat = rewConfig.getString("rank-item.no-rewards-format", " #ff3c3c• No automatic rewards");
        String statusUnlocked = rewConfig.getString("rank-item.status-unlocked", "#00ff3c✔ Unlocked");
        String statusLocked = rewConfig.getString("rank-item.status-locked", "#ff3c3c🔒 Locked (Needs {missing} Elo)");

        for (int i = 0; i < count; i++) {
            if (i >= slotsToUse.size()) break;
            int targetSlot = slotsToUse.get(i);
            if (targetSlot < 0 || targetSlot >= rows * 9) continue;

            String rankKey = rankKeys.get(i);
            int minElo = ranks.get(rankKey);
            String display = plugin.getRankManager().getRankDisplay(rankKey);
            String prefix = plugin.getRankManager().getRankPrefix(rankKey);

            String customMaterialStr = plugin.getRankManager().getRankConfig().getString("ranks." + rankKey + ".gui-material");
            Material mat = EloGui.getMaterial(customMaterialStr, null);
            if (mat == null) {
                mat = Material.WOODEN_SWORD;
                if (i >= 2) mat = Material.STONE_SWORD;
                if (i >= 4) mat = Material.IRON_SWORD;
                if (i >= 6) mat = Material.GOLDEN_SWORD;
                if (i >= 8) mat = Material.DIAMOND_SWORD;
                if (i >= 9) mat = Material.NETHERITE_SWORD;
            }

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(EloGui.colorize(itemDisplayNameTemplate.replace("{rank}", display)));
                List<String> lore = new ArrayList<>();

                String statusStr = playerElo >= minElo
                        ? statusUnlocked
                        : statusLocked.replace("{missing}", String.valueOf(minElo - playerElo));

                List<String> commands = plugin.getRankManager().getRankConfig().getStringList("ranks." + rankKey + ".rankup-commands");
                List<String> rewardLines = new ArrayList<>();
                if (commands == null || commands.isEmpty()) {
                    rewardLines.add(EloGui.colorize(noRewardsFormat));
                } else {
                    for (String cmd : commands) {
                        rewardLines.add(EloGui.colorize(rewardLineFormat.replace("{command}", cmd)));
                    }
                }

                for (String line : itemLoreTemplate) {
                    if (line.contains("{rewards}")) {
                        lore.addAll(rewardLines);
                    } else if (line.contains("{status}")) {
                        lore.add(EloGui.colorize(statusStr));
                    } else {
                        lore.add(EloGui.colorize(line
                                .replace("{min_elo}", String.valueOf(minElo))
                                .replace("{prefix}", prefix)
                        ));
                    }
                }

                meta.setLore(lore);
                int cmd = plugin.getRankManager().getRankConfig().getInt("ranks." + rankKey + ".gui-custom-model-data", -1);
                if (cmd != -1) {
                    meta.setCustomModelData(cmd);
                }
                item.setItemMeta(meta);
            }
            inv.setItem(targetSlot, item);
        }

        int backSlot = EloGui.getSlotFromLayout(rewConfig, 'a', rewConfig.getInt("back-button.slot", 40));
        if (backSlot >= 0 && backSlot < rows * 9) {
            Material mat = EloGui.getMaterial(rewConfig.getString("back-button.material"), Material.RED_STAINED_GLASS_PANE);
            ItemStack backItem = new ItemStack(mat);
            ItemMeta backMeta = backItem.getItemMeta();
            if (backMeta != null) {
                backMeta.setDisplayName(EloGui.colorize(rewConfig.getString("back-button.name", "#ff3c3c\u029c\u1d0f\u1d1c\u0274\u1d1b\u028f")));
                List<String> backLore = new ArrayList<>();
                for (String l : rewConfig.getStringList("back-button.lore")) {
                    backLore.add(EloGui.colorize(l));
                }
                backMeta.setLore(backLore);
                int cmd = rewConfig.getInt("back-button.customModelData", -1);
                if (cmd != -1) {
                    backMeta.setCustomModelData(cmd);
                }
                backItem.setItemMeta(backMeta);
            }
            inv.setItem(backSlot, backItem);
        }

        player.openInventory(inv);
    }

    public static void handleInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event, EloGui.RankRewardsHolder rankRewardsHolder, Player player, int slot, SolarElo plugin) {
        int backSlot = EloGui.getSlotFromLayout(plugin.getGuiConfigManager().getRewardsConfig(), 'a', plugin.getGuiConfigManager().getRewardsConfig().getInt("back-button.slot", 40));
        if (slot == backSlot) {
            String soundKey = plugin.getGuiConfigManager().getRewardsConfig().getString("back-button.confirm_sound", "click");
            plugin.getEffectManager().playGuiSound(player, soundKey);
            if (rankRewardsHolder.getReturnPage() == -1) {
                EloGui.openMainMenu(plugin, player);
            } else {
                EloGui.openLeaderboard(plugin, player, rankRewardsHolder.getReturnPage(), rankRewardsHolder.getReturnFilter());
            }
        }
    }

}
