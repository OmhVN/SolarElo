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
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaults);
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
