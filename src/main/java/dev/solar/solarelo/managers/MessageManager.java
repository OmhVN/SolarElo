package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageManager {

    private final SolarElo plugin;
    private FileConfiguration messages;

    public MessageManager(SolarElo plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        saveResourceIfNotExists("lang/messages_vi.yml");
        saveResourceIfNotExists("lang/messages_en.yml");
        saveResourceIfNotExists("messages.yml");

        String lang = plugin.getConfig().getString("language", "vi").toLowerCase();
        File langFile = new File(plugin.getDataFolder(), "lang/messages_" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "messages.yml");
        }

        messages = YamlConfiguration.loadConfiguration(langFile);

        InputStream defaultStream = plugin.getResource("lang/messages_" + lang + ".yml");
        if (defaultStream == null) {
            defaultStream = plugin.getResource("messages.yml");
        }
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaults);
        }
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            try {
                plugin.saveResource(resourcePath, false);
            } catch (Exception ignored) {}
        }
    }

    public String get(String key, String fallback) {
        return colorize(messages.getString(key, fallback));
    }

    public String get(String key) {
        String val = messages.getString(key);
        if (val == null) {
            plugin.getLogger().warning("Missing message key: " + key);
            return colorize("&c[missing: " + key + "]");
        }
        return colorize(val);
    }

    public String getRaw(String key, String fallback) {
        return messages.getString(key, fallback);
    }

    public String prefix() {
        return colorize(messages.getString("prefix", "&8[&eSolarElo&8] "));
    }

    private String colorize(String s) {
        return EloManager.colorize(s);
    }

    public void send(org.bukkit.command.CommandSender sender, String key) {
        String msg = get(key);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(msg);
        }
    }

    public void send(org.bukkit.command.CommandSender sender, String key, String fallback) {
        String msg = get(key, fallback);
        if (msg != null && !msg.isEmpty()) {
            sender.sendMessage(msg);
        }
    }

    public static void sendMessage(org.bukkit.command.CommandSender sender, String msg) {
        if (sender != null && msg != null && !msg.isEmpty()) {
            sender.sendMessage(msg);
        }
    }
}
