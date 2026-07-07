package dev.solar.solarelo.listeners;

import dev.solar.solarelo.SolarElo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {

    private final SolarElo plugin;

    public PlayerJoinQuitListener(SolarElo plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        plugin.getEloManager().loadPlayer(player);
        plugin.getEloManager().initializeActivityData(player);

        if (player.hasPermission("solarelo.admin")) {
            plugin.runDelayedForEntity(player, () -> {
                plugin.getUpdateManager().sendUpdateNotification(player);
            }, 40L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getEloManager().unloadPlayer(event.getPlayer().getUniqueId());
        plugin.getEloManager().removeActivityData(event.getPlayer().getUniqueId());
        dev.solar.solarelo.listeners.GuiListener.chatPrompts.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        plugin.runDelayedForEntity(player, () -> {
            plugin.getEloManager().triggerPendingDeathNotifications(player);
        }, 10L);
    }
}
