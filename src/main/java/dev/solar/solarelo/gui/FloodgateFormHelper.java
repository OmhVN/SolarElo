package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class FloodgateFormHelper {

    private static Boolean floodgateAvailable = null;

    private static boolean isFloodgateAvailable() {
        if (floodgateAvailable == null) {
            try {
                floodgateAvailable = Bukkit.getPluginManager().isPluginEnabled("floodgate")
                        || Bukkit.getPluginManager().isPluginEnabled("Floodgate");
            } catch (Throwable t) {
                floodgateAvailable = false;
            }
        }
        return floodgateAvailable;
    }

    public static boolean isBedrockPlayer(Player player) {
        if (!isFloodgateAvailable()) return false;
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            return (boolean) api.getClass().getMethod("isFloodgatePlayer", UUID.class)
                    .invoke(api, player.getUniqueId());
        } catch (Throwable t) {
            return false;
        }
    }

    public static void showSearchForm(SolarElo plugin, Player admin) {
        if (!isFloodgateAvailable() || !isBedrockPlayer(admin)) {
            triggerChatSearch(plugin, admin);
            return;
        }

        try {
            String title       = plugin.getMessageManager().get("search-form-title", "Tìm kiếm người chơi");
            String label       = plugin.getMessageManager().get("search-form-label", "Nhập tên người chơi:");
            String placeholder = plugin.getMessageManager().get("search-form-placeholder", "Tên...");

            Class<?> customFormClass = Class.forName("org.geysermc.cumulus.form.CustomForm");
            Method builderMethod = customFormClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            Class<?> builderClass = builder.getClass();
            builder = builderClass.getMethod("title", String.class).invoke(builder, title);
            builder = builderClass.getMethod("input", String.class, String.class, String.class)
                    .invoke(builder, label, placeholder, "");

            try {

                Class<?> responseClass = Class.forName("org.geysermc.cumulus.response.CustomFormResponse");
                Method handlerMethod = builderClass.getMethod("validResultHandler", java.util.function.Consumer.class);

                java.util.function.Consumer<Object> consumer = (response) -> {
                    try {
                        String rawInput = (String) responseClass.getMethod("asInput", int.class).invoke(response, 0);
                        String input = (rawInput != null ? rawInput : "").trim();
                        if (input.isEmpty() || input.equalsIgnoreCase("cancel")) {
                            admin.sendMessage(EloGui.colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &7Đã hủy tìm kiếm."));
                            plugin.runSync(() -> EloGui.openEloAdmin(plugin, admin));
                            return;
                        }
                        plugin.runAsync(() -> resolveAndOpen(plugin, admin, input));
                    } catch (Throwable ex) {
                        plugin.getLogger().warning("[SolarElo] Error handling Bedrock form response: " + ex.getMessage());
                    }
                };

                handlerMethod.invoke(builder, (java.util.function.Consumer) consumer);

            } catch (NoSuchMethodException e) {

                plugin.getLogger().warning("[SolarElo] Cumulus validResultHandler not found, falling back to chat prompt.");
                triggerChatSearch(plugin, admin);
                return;
            }

            Object form = builderClass.getMethod("build").invoke(builder);

            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object floodgateApi = floodgateApiClass.getMethod("getInstance").invoke(null);
            Class<?> formInterface = Class.forName("org.geysermc.cumulus.form.Form");
            floodgateApiClass.getMethod("sendForm", UUID.class, formInterface)
                    .invoke(floodgateApi, admin.getUniqueId(), form);

        } catch (Throwable t) {
            plugin.getLogger().warning("[SolarElo] FloodgateForm failed (" + t.getMessage() + "), falling back to chat prompt.");
            triggerChatSearch(plugin, admin);
        }
    }

    public static void triggerChatSearch(SolarElo plugin, Player admin) {
        String promptMsg = plugin.getMessageManager().get(
                "search-chat-prompt",
                "&#ffaa00ᴇʟᴏ ᴀᴅᴍɪɴ &8» &fNhập tên người chơi cần tìm &7(gõ &ccancel &7để hủy):"
        );
        admin.sendMessage(EloGui.colorize(promptMsg));
        dev.solar.solarelo.listeners.GuiListener.chatPrompts.put(
                admin.getUniqueId(),
                new dev.solar.solarelo.listeners.GuiListener.ChatPromptData(null, null, "search")
        );
    }

    public static void resolveAndOpen(SolarElo plugin, Player admin, String playerName) {
        Player online = Bukkit.getPlayerExact(playerName);
        UUID uuid;
        String name;

        if (online != null) {
            uuid = online.getUniqueId();
            name = online.getName();
        } else {
            PlayerData offline = plugin.getDatabaseManager().getPlayerByName(playerName);
            if (offline == null) {
                String notFound = plugin.getMessageManager().get(
                        "player-not-found",
                        "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi: &f{name}"
                ).replace("{name}", playerName);
                admin.sendMessage(EloGui.colorize(notFound));
                plugin.runSync(() -> EloGui.openEloAdmin(plugin, admin));
                return;
            }
            uuid = offline.getUuid();
            name = offline.getName();
        }

        plugin.runSync(() -> EloGui.openEloAdminDetail(plugin, admin, uuid, name));
    }
}
