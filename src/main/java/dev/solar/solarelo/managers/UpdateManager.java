package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateManager {

    private final SolarElo plugin;
    private static final String PROJECT_ID = "5ngmjJm3";
    private static final String PROJECT_URL = "https://modrinth.com/plugin/" + PROJECT_ID;

    private String latestVersion = null;
    private boolean updateAvailable = false;

    public UpdateManager(SolarElo plugin) {
        this.plugin = plugin;
    }

    public void checkUpdateAsync() {
        plugin.runAsync(() -> {
            try {
                String url = "https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version";

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                String userAgent = "OmhVN/SolarElo/" + plugin.getDescription().getVersion() + " (omhvn@solar.dev)";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", userAgent)
                        .header("Accept", "application/json")
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String json = response.body();

                    Pattern pattern = Pattern.compile("\"version_number\"\\s*:\\s*\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(json);

                    if (matcher.find()) {
                        String latest = matcher.group(1);
                        String current = plugin.getDescription().getVersion();

                        if (isNewerVersion(current, latest)) {
                            this.latestVersion = latest;
                            this.updateAvailable = true;

                            plugin.getLogger().warning("--------------------------------------------------");
                            plugin.getLogger().warning(" SolarElo Update: Version v" + latest + " is available!");
                            plugin.getLogger().warning(" You are running: v" + current);
                            plugin.getLogger().warning(" Download link: " + PROJECT_URL);
                            plugin.getLogger().warning("--------------------------------------------------");
                        } else {
                            plugin.getLogger().info("SolarElo is running the latest version (v" + current + ").");
                        }
                    } else {
                        plugin.getLogger().warning("Could not parse version information from Modrinth API.");
                    }
                } else {
                    plugin.getLogger().warning("Failed to check for updates from Modrinth API. Status code: " + response.statusCode());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking for updates from Modrinth: " + e.getMessage());
            }
        });
    }

    public void sendUpdateNotification(Player player) {
        if (!updateAvailable || latestVersion == null) {
            return;
        }

        String current = plugin.getDescription().getVersion();

        String part1 = EloManager.colorize("&6[SolarElo] &fA new version is available: &av" + latestVersion + " &7(Current: v" + current + ")&f. Update now on Modrinth: &b");
        String linkText = EloManager.colorize("&b[Click here]");
        String hoverText = EloManager.colorize("&7Latest version: &av" + latestVersion + "\n&7Current version: &cv" + current + "\n\n&bClick to open the Modrinth download page.");

        TextComponent message = new TextComponent();

        for (BaseComponent c : TextComponent.fromLegacyText(part1)) {
            message.addExtra(c);
        }

        TextComponent linkComponent = new TextComponent(linkText);
        linkComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new BaseComponent[] { new TextComponent(hoverText) }
        ));
        linkComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PROJECT_URL));
        message.addExtra(linkComponent);

        player.spigot().sendMessage(message);
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getProjectUrl() {
        return PROJECT_URL;
    }

    public static boolean isNewerVersion(String currentStr, String latestStr) {
        if (currentStr == null || latestStr == null) return false;
        if (currentStr.equalsIgnoreCase(latestStr)) return false;

        String c = normalizeVersion(currentStr);
        String l = normalizeVersion(latestStr);

        String[] cParts = c.split("[._-]");
        String[] lParts = l.split("[._-]");

        int length = Math.max(cParts.length, lParts.length);
        for (int i = 0; i < length; i++) {
            String cPart = i < cParts.length ? cParts[i] : "0";
            String lPart = i < lParts.length ? lParts[i] : "0";

            int comp = compareVersionParts(cPart, lPart);
            if (comp < 0) return true;
            if (comp > 0) return false;
        }
        return false;
    }

    private static String normalizeVersion(String v) {
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1);
        }
        return v.replace("-R", ".").replace("-r", ".").replace(" ", "");
    }

    private static int compareVersionParts(String cPart, String lPart) {
        boolean cIsNum = isNumeric(cPart);
        boolean lIsNum = isNumeric(lPart);

        if (cIsNum && lIsNum) {
            return Integer.compare(Integer.parseInt(cPart), Integer.parseInt(lPart));
        }

        boolean cIsDev = cPart.equalsIgnoreCase("SNAPSHOT") || cPart.equalsIgnoreCase("DEV");
        boolean lIsDev = lPart.equalsIgnoreCase("SNAPSHOT") || lPart.equalsIgnoreCase("DEV");

        if (cIsDev && !lIsDev) {
            return -1;
        }
        if (!cIsDev && lIsDev) {
            return 1;
        }

        return cPart.compareToIgnoreCase(lPart);
    }

    private static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
