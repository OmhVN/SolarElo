package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.managers.EloManager;
import dev.solar.solarelo.managers.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EloGui {

    public static class LeaderboardHolder implements InventoryHolder {
        private final int page;
        private final String filter;
        private Inventory inventory;

        public LeaderboardHolder(int page, String filter) {
            this.page = page;
            this.filter = filter;
        }

        public int getPage() { return page; }
        public String getFilter() { return filter; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class RankRewardsHolder implements InventoryHolder {
        private final int returnPage;
        private final String returnFilter;
        private Inventory inventory;

        public RankRewardsHolder(int returnPage, String returnFilter) {
            this.returnPage = returnPage;
            this.returnFilter = returnFilter;
        }

        public int getReturnPage() { return returnPage; }
        public String getReturnFilter() { return returnFilter; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class StatsHolder implements InventoryHolder {
        private final String targetPlayerName;
        private final int returnPage;
        private final String returnFilter;
        private Inventory inventory;

        public StatsHolder(String targetPlayerName, int returnPage, String returnFilter) {
            this.targetPlayerName = targetPlayerName;
            this.returnPage = returnPage;
            this.returnFilter = returnFilter;
        }

        public String getTargetPlayerName() { return targetPlayerName; }
        public int getReturnPage() { return returnPage; }
        public String getReturnFilter() { return returnFilter; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class BountyHolder implements InventoryHolder {
        private final int page;
        private final String filter;
        private Inventory inventory;

        public BountyHolder(int page, String filter) {
            this.page = page;
            this.filter = filter;
        }

        public int getPage() { return page; }
        public String getFilter() { return filter; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class BountyConfirmHolder implements InventoryHolder {
        private final UUID targetUuid;
        private final String targetName;
        private Inventory inventory;

        public BountyConfirmHolder(UUID targetUuid, String targetName) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
        }

        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetName() { return targetName; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class ActiveQuestHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class MainMenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class SettingsHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class EloAdminHolder implements InventoryHolder {
        private final int page;
        private Inventory inventory;

        public EloAdminHolder(int page) {
            this.page = page;
        }

        public int getPage() { return page; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class EloAdminDetailHolder implements InventoryHolder {
        private final UUID targetUuid;
        private final String targetName;
        private Inventory inventory;

        public EloAdminDetailHolder(UUID targetUuid, String targetName) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
        }

        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetName() { return targetName; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class EloHistoryHolder implements InventoryHolder {
        private final UUID targetUuid;
        private final String targetName;
        private final int page;
        private final String filter;
        private Inventory inventory;

        public EloHistoryHolder(UUID targetUuid, String targetName, int page, String filter) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.page = page;
            this.filter = filter;
        }

        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetName() { return targetName; }
        public int getPage() { return page; }
        public String getFilter() { return filter; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class KillHistoryHolder implements InventoryHolder {
        private final UUID targetUuid;
        private final String targetName;
        private final int page;
        private final String filter;
        private Inventory inventory;

        public KillHistoryHolder(UUID targetUuid, String targetName, int page, String filter) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.page = page;
            this.filter = filter;
        }

        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetName() { return targetName; }
        public int getPage() { return page; }
        public String getFilter() { return filter; }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static void openLeaderboard(SolarElo plugin, Player player, int page, String filter) {
        if (!plugin.getGuiConfigManager().getLeaderboardConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-leaderboard", "&#ff3c3cTính năng Bảng xếp hạng hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        LeaderboardMenu.open(plugin, player, page, filter);
    }

    public static void openRankRewards(SolarElo plugin, Player player, int returnPage, String returnFilter) {
        if (!plugin.getGuiConfigManager().getRewardsConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-rewards", "&#ff3c3cTính năng Phần thưởng hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        RankRewardsMenu.open(plugin, player, returnPage, returnFilter);
    }

    public static void openStats(SolarElo plugin, Player player, String targetPlayerName, int returnPage, String returnFilter) {
        if (!plugin.getGuiConfigManager().getStatsConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-stats", "&#ff3c3cTính năng Xem thống kê bằng giao diện hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        StatsMenu.open(plugin, player, targetPlayerName, returnPage, returnFilter);
    }

    public static void openBounty(SolarElo plugin, Player player) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        BountyMenu.open(plugin, player);
    }

    public static void openBounty(SolarElo plugin, Player player, int page, String filter) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        BountyMenu.open(plugin, player, page, filter);
    }

    public static void openBountyConfirm(SolarElo plugin, Player player, UUID targetUuid, String targetName) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        OtherMenus.openBountyConfirm(plugin, player, targetUuid, targetName);
    }

    public static void openActiveQuest(SolarElo plugin, Player player) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        OtherMenus.openActiveQuest(plugin, player);
    }

    public static void openMainMenu(SolarElo plugin, Player player) {
        OtherMenus.openMainMenu(plugin, player);
    }

    public static void openSettings(SolarElo plugin, Player player) {
        if (!plugin.getGuiConfigManager().getSettingsConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-settings", "&#ff3c3cTính năng Cài đặt hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        OtherMenus.openSettings(plugin, player);
    }

    public static void openEloAdmin(SolarElo plugin, Player player) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openEloAdmin(plugin, player);
    }

    public static void openEloAdmin(SolarElo plugin, Player player, int page) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openEloAdmin(plugin, player, page);
    }

    public static void openEloAdminDetail(SolarElo plugin, Player player, UUID targetUuid, String targetName) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openEloAdminDetail(plugin, player, targetUuid, targetName);
    }

    public static void openEloHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openEloHistory(plugin, player, targetUuid, targetName, page);
    }

    public static void openEloHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page, String filter) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openEloHistory(plugin, player, targetUuid, targetName, page, filter);
    }

    public static void openKillHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openKillHistory(plugin, player, targetUuid, targetName, page);
    }

    public static void openKillHistory(SolarElo plugin, Player player, UUID targetUuid, String targetName, int page, String filter) {
        if (!plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-admin", "&#ff3c3cTính năng Quản trị hiện đang bị tắt.");
            player.sendMessage(colorize(msg));
            return;
        }
        AdminMenu.openKillHistory(plugin, player, targetUuid, targetName, page, filter);
    }

    public static void playClickSound(SolarElo plugin, Player player) {
        EffectManager effectManager = plugin.getEffectManager();
        if (effectManager.isGuiClickSoundEnabled()) {
            String soundName = effectManager.getGuiClickSoundName();
            float volume = (float) effectManager.getGuiClickSoundVolume();
            float pitch = (float) effectManager.getGuiClickSoundPitch();
            try {
                org.bukkit.Sound sound = EffectManager.matchSound(soundName);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid GUI click sound configured: " + soundName);
            }
        }
    }

    public static Material getMaterial(String materialName, Material fallback) {
        if (materialName == null || materialName.isEmpty()) return fallback;
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public static String getPositionColor(SolarElo plugin, int pos) {
        org.bukkit.configuration.file.FileConfiguration guiConfig = plugin.getGuiConfigManager().getLeaderboardConfig();
        if (pos <= 0) {
            return guiConfig.getString("rank-colors.default", "#ffffff");
        }
        org.bukkit.configuration.ConfigurationSection section = guiConfig.getConfigurationSection("rank-colors");
        if (section == null) {
            return "#ffffff";
        }

        int bestThreshold = Integer.MAX_VALUE;
        String bestColor = null;

        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase("default")) {
                continue;
            }
            int threshold = -1;
            String cleanKey = key.toLowerCase();
            if (cleanKey.startsWith("top-")) {
                try {
                    threshold = Integer.parseInt(cleanKey.substring(4));
                } catch (NumberFormatException ignored) {}
            } else {
                try {
                    threshold = Integer.parseInt(cleanKey);
                } catch (NumberFormatException ignored) {}
            }

            if (threshold != -1 && pos <= threshold && threshold < bestThreshold) {
                bestThreshold = threshold;
                bestColor = section.getString(key);
            }
        }

        if (bestColor != null) {
            return bestColor;
        }

        return section.getString("default", "#ffffff");
    }

    public static String colorize(String s) {
        return EloManager.colorize(s);
    }

    public static Inventory createInventory(InventoryHolder holder, int size, String title) {
        return Bukkit.createInventory(holder, size, "§0§1§2§3§r" + title);
    }

    public static String formatTimeRemaining(long seconds) {
        if (seconds <= 0) return "0s";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        if (h > 0) {
            return String.format("%dh %dm %ds", h, m, s);
        } else if (m > 0) {
            return String.format("%dm %ds", m, s);
        } else {
            return String.format("%ds", s);
        }
    }

    public static String formatNumber(double value) {
        if (value == 0) return "0";
        boolean negative = value < 0;
        double absValue = Math.abs(value);

        String formatted;
        if (absValue < 1000) {
            if (absValue == (long) absValue) {
                formatted = String.valueOf((long) absValue);
            } else {
                java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(java.util.Locale.US);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#.##", symbols);
                formatted = df.format(absValue);
            }
        } else {
            String[] suffixes = new String[]{"", "K", "M", "B", "T"};
            int index = 0;
            double d = absValue;
            while (d >= 1000 && index < suffixes.length - 1) {
                d /= 1000;
                index++;
            }
            java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(java.util.Locale.US);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.#", symbols);
            formatted = df.format(d) + suffixes[index];
        }
        return negative ? "-" + formatted : formatted;
    }

    public static boolean checkIpBlockedRedirect(SolarElo plugin, Player player, boolean isMainMenu) {
        if (plugin.getEloManager().isIpBlocked(player)) {
            if (!isMainMenu) {
                plugin.runSync(() -> openMainMenu(plugin, player));
            }
            return true;
        }
        return false;
    }

    public static ItemStack loadConfigItem(org.bukkit.configuration.file.FileConfiguration config, String path, String defaultMaterial, String defaultName, List<String> defaultLore, int defaultCmd) {
        String matStr = config.getString(path + ".material", defaultMaterial);
        Material mat = Material.matchMaterial(matStr);
        if (mat == null) mat = Material.valueOf(defaultMaterial);

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(config.getString(path + ".name", defaultName)));
            List<String> lore = config.getStringList(path + ".lore");
            if (lore == null || lore.isEmpty()) {
                lore = defaultLore;
            }
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(colorize(line));
            }
            meta.setLore(coloredLore);
            int cmd = config.getInt(path + ".customModelData", defaultCmd);
            if (cmd != -1) meta.setCustomModelData(cmd);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static List<Integer> getSlotsFromLayout(org.bukkit.configuration.file.FileConfiguration config, String path, char symbol) {
        List<Integer> slots = new ArrayList<>();
        List<String> disposition = config.getStringList(path);
        if (disposition == null || disposition.isEmpty()) {
            return slots;
        }
        for (int r = 0; r < disposition.size(); r++) {
            String rowStr = disposition.get(r);
            for (int c = 0; c < rowStr.length() && c < 9; c++) {
                if (rowStr.charAt(c) == symbol) {
                    slots.add(r * 9 + c);
                }
            }
        }
        return slots;
    }

    public static List<Integer> getSlotsFromLayout(org.bukkit.configuration.file.FileConfiguration config, char symbol) {
        return getSlotsFromLayout(config, "gui-disposition", symbol);
    }

    public static int getSlotFromLayout(org.bukkit.configuration.file.FileConfiguration config, String path, char symbol, int defaultSlot) {
        List<Integer> slots = getSlotsFromLayout(config, path, symbol);
        return slots.isEmpty() ? defaultSlot : slots.get(0);
    }

    public static int getSlotFromLayout(org.bukkit.configuration.file.FileConfiguration config, char symbol, int defaultSlot) {
        return getSlotFromLayout(config, "gui-disposition", symbol, defaultSlot);
    }
}
