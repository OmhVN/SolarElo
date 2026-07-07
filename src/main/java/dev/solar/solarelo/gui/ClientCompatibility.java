package dev.solar.solarelo.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;

public class ClientCompatibility {

    private static Boolean viaVersionEnabled = null;
    private static Boolean geyserEnabled = null;

    private static boolean isViaVersionEnabled() {
        if (viaVersionEnabled == null) {
            try {
                viaVersionEnabled = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
            } catch (Throwable t) {
                viaVersionEnabled = false;
            }
        }
        return viaVersionEnabled;
    }

    private static boolean isGeyserEnabled() {
        if (geyserEnabled == null) {
            try {
                geyserEnabled = Bukkit.getPluginManager().isPluginEnabled("Geyser-Spigot");
            } catch (Throwable t) {
                geyserEnabled = false;
            }
        }
        return geyserEnabled;
    }

    public static boolean supportsDialog(Player player) {
        UUID uuid = player.getUniqueId();

        if (isGeyserEnabled()) {
            try {
                Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
                Object api = geyserApiClass.getMethod("api").invoke(null);
                boolean isBedrock = (boolean) api.getClass().getMethod("isBedrockPlayer", UUID.class).invoke(api, uuid);
                if (isBedrock) {
                    return false;
                }
            } catch (Throwable ignored) {}
        }

        if (isViaVersionEnabled()) {
            try {
                Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
                Object api = viaClass.getMethod("getAPI").invoke(null);
                int protocolVersion = (int) api.getClass().getMethod("getPlayerVersion", UUID.class).invoke(api, uuid);
                if (protocolVersion < 769) {
                    return false;
                }
            } catch (Throwable ignored) {}
        }

        return true;
    }
}
