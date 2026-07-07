package dev.solar.solarelo.listeners;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.gui.EloGui;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    private final SolarElo plugin;

    public static final java.util.Map<UUID, ChatPromptData> chatPrompts = new java.util.concurrent.ConcurrentHashMap<>();

    public static class ChatPromptData {
        private final UUID targetUuid;
        private final String targetName;
        private final String action;

        public ChatPromptData(UUID targetUuid, String targetName, String action) {
            this.targetUuid = targetUuid;
            this.targetName = targetName;
            this.action = action;
        }

        public UUID getTargetUuid() { return targetUuid; }
        public String getTargetName() { return targetName; }
        public String getAction() { return action; }
    }

    private void registerChatPrompt(Player admin, UUID targetUuid, String targetName, String action) {
        chatPrompts.put(admin.getUniqueId(), new ChatPromptData(targetUuid, targetName, action));
    }

    public GuiListener(SolarElo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        boolean isCustomGui = holder instanceof EloGui.LeaderboardHolder
                || holder instanceof EloGui.RankRewardsHolder
                || holder instanceof EloGui.StatsHolder
                || holder instanceof EloGui.BountyHolder
                || holder instanceof EloGui.BountyConfirmHolder
                || holder instanceof EloGui.ActiveQuestHolder
                || holder instanceof EloGui.MainMenuHolder
                || holder instanceof EloGui.SettingsHolder
                || holder instanceof EloGui.EloAdminHolder
                || holder instanceof EloGui.EloAdminDetailHolder
                || holder instanceof EloGui.EloHistoryHolder
                || holder instanceof EloGui.KillHistoryHolder;

        if (!isCustomGui) return;

        event.setCancelled(true);
        plugin.runSync(player::updateInventory);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        if (holder instanceof EloGui.LeaderboardHolder leaderboardHolder) {
            dev.solar.solarelo.gui.LeaderboardMenu.handleInventoryClick(event, leaderboardHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.RankRewardsHolder rankRewardsHolder) {
            dev.solar.solarelo.gui.RankRewardsMenu.handleInventoryClick(event, rankRewardsHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.StatsHolder statsHolder) {
            dev.solar.solarelo.gui.StatsMenu.handleInventoryClick(event, statsHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.BountyHolder bountyHolder) {
            dev.solar.solarelo.gui.BountyMenu.handleInventoryClick(event, bountyHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.ActiveQuestHolder) {
            dev.solar.solarelo.gui.BountyMenu.handleActiveQuestClick(event, player, slot, plugin);
        } else if (holder instanceof EloGui.BountyConfirmHolder confirmHolder) {
            dev.solar.solarelo.gui.BountyMenu.handleBountyConfirmClick(event, confirmHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.MainMenuHolder) {
            dev.solar.solarelo.gui.OtherMenus.handleMainMenuClick(event, player, slot, plugin);
        } else if (holder instanceof EloGui.SettingsHolder) {
            dev.solar.solarelo.gui.OtherMenus.handleSettingsClick(event, player, slot, plugin);
        } else if (holder instanceof EloGui.EloAdminHolder adminHolder) {
            dev.solar.solarelo.gui.AdminMenu.handleEloAdminClick(event, adminHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.EloAdminDetailHolder detailHolder) {
            dev.solar.solarelo.gui.AdminMenu.handleEloAdminDetailClick(event, detailHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.EloHistoryHolder historyHolder) {
            dev.solar.solarelo.gui.AdminMenu.handleEloHistoryClick(event, historyHolder, player, slot, plugin);
        } else if (holder instanceof EloGui.KillHistoryHolder killHistoryHolder) {
            dev.solar.solarelo.gui.AdminMenu.handleKillHistoryClick(event, killHistoryHolder, player, slot, plugin);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof EloGui.LeaderboardHolder
                || holder instanceof EloGui.RankRewardsHolder
                || holder instanceof EloGui.StatsHolder
                || holder instanceof EloGui.BountyHolder
                || holder instanceof EloGui.BountyConfirmHolder
                || holder instanceof EloGui.ActiveQuestHolder
                || holder instanceof EloGui.MainMenuHolder
                || holder instanceof EloGui.SettingsHolder
                || holder instanceof EloGui.EloAdminHolder
                || holder instanceof EloGui.EloAdminDetailHolder
                || holder instanceof EloGui.EloHistoryHolder
                || holder instanceof EloGui.KillHistoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onPlayerChatPrompt(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player admin = event.getPlayer();
        ChatPromptData data = chatPrompts.remove(admin.getUniqueId());
        if (data == null) return;

        event.setCancelled(true);
        String msg = event.getMessage().trim();

        if (msg.equalsIgnoreCase("cancel")) {
            admin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &7Đã hủy yêu cầu chỉnh sửa."));
            if ("search".equalsIgnoreCase(data.getAction())) {
                plugin.runSync(() -> EloGui.openEloAdmin(plugin, admin));
            } else {
                plugin.runSync(() -> EloGui.openEloAdminDetail(plugin, admin, data.getTargetUuid(), data.getTargetName()));
            }
            return;
        }

        if ("search".equalsIgnoreCase(data.getAction())) {
            final String playerName = msg;
            plugin.runAsync(() -> dev.solar.solarelo.gui.FloodgateFormHelper.resolveAndOpen(plugin, admin, playerName));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(msg);
        } catch (NumberFormatException e) {
            admin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cGiá trị nhập vào phải là số nguyên! &7Thử lại hoặc gõ &#ff3c3ccancel&7 để hủy."));
            chatPrompts.put(admin.getUniqueId(), data);
            return;
        }

        plugin.runSync(() -> {
            switch (data.getAction()) {
                case "set" -> {
                    plugin.getEloManager().setElo(data.getTargetUuid(), data.getTargetName(), amount);
                    admin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã đặt ELO của &#ffffff" + data.getTargetName() + " &fthành &#00ff3c" + amount + " ELO&f."));
                }
                case "add" -> {
                    plugin.getEloManager().addElo(data.getTargetUuid(), data.getTargetName(), amount);
                    admin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã cộng &#00ff3c+" + amount + " ELO &fcho &#ffffff" + data.getTargetName()));
                }
                case "remove" -> {
                    plugin.getEloManager().removeElo(data.getTargetUuid(), data.getTargetName(), amount);
                    admin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã trừ &#ff3c3c-" + amount + " ELO &fcủa &#ffffff" + data.getTargetName()));
                }
            }
            EloGui.openEloAdminDetail(plugin, admin, data.getTargetUuid(), data.getTargetName());
        });
    }
}
