package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WebhookManager {

    private final SolarElo plugin;
    private final HttpClient httpClient;

    public WebhookManager(SolarElo plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void sendKill(String killerName, int killerElo, String victimName, int victimElo) {
        if (!plugin.getDiscordConfig().getBoolean("discord-webhook.enabled", false)) return;
        if (!plugin.getDiscordConfig().getBoolean("discord-webhook.events.kill.enabled", true)) return;

        String format = plugin.getDiscordConfig().getString("discord-webhook.events.kill.format",
                "⚔️ **{killer}** ({killer_elo} Elo) has defeated **{victim}** ({victim_elo} Elo)!");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("{killer}", killerName);
        replacements.put("{killer_elo}", String.valueOf(killerElo));
        replacements.put("{victim}", victimName);
        replacements.put("{victim_elo}", String.valueOf(victimElo));

        org.bukkit.configuration.ConfigurationSection embedSection = plugin.getDiscordConfig().getConfigurationSection("discord-webhook.events.kill.embed");
        sendWebhook(format, embedSection, replacements);
    }

    public void sendTop1Defeat(String killerName, String victimName) {
        if (!plugin.getDiscordConfig().getBoolean("discord-webhook.enabled", false)) return;
        if (!plugin.getDiscordConfig().getBoolean("discord-webhook.events.top-1-defeat.enabled", true)) return;

        String format = plugin.getDiscordConfig().getString("discord-webhook.events.top-1-defeat.format",
                "👑 **{killer}** has defeated the #1 ranked player **{victim}**!");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("{killer}", killerName);
        replacements.put("{victim}", victimName);

        org.bukkit.configuration.ConfigurationSection embedSection = plugin.getDiscordConfig().getConfigurationSection("discord-webhook.events.top-1-defeat.embed");
        sendWebhook(format, embedSection, replacements);
    }

    public void sendRankUp(String playerName, String rankDisplay) {
        if (!plugin.getDiscordConfig().getBoolean("discord-webhook.enabled", false)) return;
        if (!plugin.getDiscordConfig().getBoolean("discord-webhook.events.rank-up.enabled", true)) return;

        String cleanRank = rankDisplay.replaceAll("(?i)§[0-9a-fk-orx]", "");

        String format = plugin.getDiscordConfig().getString("discord-webhook.events.rank-up.format",
                "⚡ **{player}** has ranked up to **{rank}**!");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("{player}", playerName);
        replacements.put("{rank}", cleanRank);

        org.bukkit.configuration.ConfigurationSection embedSection = plugin.getDiscordConfig().getConfigurationSection("discord-webhook.events.rank-up.embed");
        sendWebhook(format, embedSection, replacements);
    }

    private void sendWebhook(String fallbackContent, org.bukkit.configuration.ConfigurationSection embedSection, java.util.Map<String, String> replacements) {
        String url = plugin.getDiscordConfig().getString("discord-webhook.url", "");
        if (url == null || url.isEmpty() || url.equals("https://discord.com/api/webhooks/...")) return;

        boolean useEmbed = false;
        if (embedSection != null && embedSection.getParent() != null) {
            useEmbed = embedSection.getParent().getBoolean("use-embed", false);
        }

        String json;
        if (useEmbed && embedSection != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"embeds\": [{");

            boolean hasField = false;

            String title = embedSection.getString("title");
            if (title != null && !title.isEmpty()) {
                title = replaceAll(title, replacements);
                sb.append("\"title\": \"").append(escapeJson(title)).append("\"");
                hasField = true;
            }

            String desc = embedSection.getString("description");
            if (desc != null && !desc.isEmpty()) {
                desc = replaceAll(desc, replacements);
                if (hasField) sb.append(", ");
                sb.append("\"description\": \"").append(escapeJson(desc)).append("\"");
                hasField = true;
            }

            String colorStr = embedSection.getString("color");
            if (colorStr != null && !colorStr.isEmpty()) {
                int colorInt = parseColor(colorStr);
                if (hasField) sb.append(", ");
                sb.append("\"color\": ").append(colorInt);
                hasField = true;
            }

            String footer = embedSection.getString("footer");
            if (footer != null && !footer.isEmpty()) {
                footer = replaceAll(footer, replacements);
                if (hasField) sb.append(", ");
                sb.append("\"footer\": {\"text\": \"").append(escapeJson(footer)).append("\"}");
                hasField = true;
            }

            if (embedSection.getBoolean("timestamp", false)) {
                String timestampStr = java.time.Instant.now().toString();
                if (hasField) sb.append(", ");
                sb.append("\"timestamp\": \"").append(timestampStr).append("\"");
                hasField = true;
            }

            String thumbnail = embedSection.getString("thumbnail");
            if (thumbnail != null && !thumbnail.isEmpty()) {
                thumbnail = replaceAll(thumbnail, replacements);
                if (hasField) sb.append(", ");
                sb.append("\"thumbnail\": {\"url\": \"").append(escapeJson(thumbnail)).append("\"}");
                hasField = true;
            }

            sb.append("}]}");
            json = sb.toString();
        } else {
            String content = replaceAll(fallbackContent, replacements);
            String escaped = escapeJson(content);
            json = "{\"content\": \"" + escaped + "\"}";
        }

        plugin.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "SolarElo-Plugin")
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                                plugin.getLogger().warning("Discord Webhook returned status code: " + response.statusCode() + " for payload: " + json);
                            }
                        })
                        .exceptionally(ex -> {
                            plugin.getLogger().warning("Failed to send Discord Webhook: " + ex.getMessage());
                            return null;
                        });
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to construct Discord Webhook request: " + e.getMessage());
            }
        });
    }

    private String replaceAll(String template, java.util.Map<String, String> replacements) {
        if (template == null) return "";
        String result = template;
        for (java.util.Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private int parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return 0;
        try {
            String cleaned = colorStr.trim();
            if (cleaned.startsWith("#")) {
                cleaned = cleaned.substring(1);
            } else if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) {
                cleaned = cleaned.substring(2);
            }
            return Integer.parseInt(cleaned, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
