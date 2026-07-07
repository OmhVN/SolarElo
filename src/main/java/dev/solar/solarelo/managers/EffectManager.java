package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EffectManager {

    private final SolarElo plugin;
    private FileConfiguration config;

    public EffectManager(SolarElo plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "effects.yml");
        if (!file.exists()) {
            plugin.saveResource("effects.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource("effects.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaults);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isGuiClickSoundEnabled() {
        return config.getBoolean("click.enabled", true);
    }

    public String getGuiClickSoundName() {
        String s = config.getString("click.sound", "UI_BUTTON_CLICK");
        return s == null ? "UI_BUTTON_CLICK" : s.trim();
    }

    public double getGuiClickSoundVolume() {
        return config.getDouble("click.volume", 1.0);
    }

    public double getGuiClickSoundPitch() {
        return config.getDouble("click.pitch", 1.0);
    }

    public void playGuiSound(org.bukkit.entity.Player player, String soundKey) {
        if (player == null || !player.isOnline()) return;

        String key = (soundKey == null || soundKey.isEmpty()) ? "click" : soundKey;

        if (!config.contains(key)) {
            key = "click";
        }

        if (config.getBoolean(key + ".enabled", true)) {
            String soundName = config.getString(key + ".sound");
            if (soundName != null && !soundName.trim().isEmpty()) {
                soundName = soundName.trim();
                double volume = config.getDouble(key + ".volume", 1.0);
                double pitch = config.getDouble(key + ".pitch", 1.0);
                try {
                    org.bukkit.Sound sound = matchSound(soundName);
                    plugin.runForEntity(player, () -> player.playSound(player.getLocation(), sound, (float) volume, (float) pitch));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid GUI sound configured for " + key + ": " + soundName);
                }
            }
        }
    }

    public String getEffectSoundString(String key, String path, String defaultValue) {
        String val = config.getString("effects." + path + "." + key);
        if (val == null || val.isEmpty()) {
            if (path.equals("plus")) {
                val = config.getString("effects.kill." + key, defaultValue);
            } else if (path.equals("minus")) {
                val = config.getString("effects.death." + key, defaultValue);
            } else {
                val = defaultValue;
            }
        }
        return val == null ? defaultValue : val.trim();
    }

    public double getEffectSoundDouble(String key, String path, double defaultValue) {
        if (!config.contains("effects." + path + "." + key)) {
            if (path.equals("plus")) {
                return config.getDouble("effects.kill." + key, defaultValue);
            } else if (path.equals("minus")) {
                return config.getDouble("effects.death." + key, defaultValue);
            }
        }
        return config.getDouble("effects." + path + "." + key, defaultValue);
    }

    public String getEffectParticleString(String key, String path, String defaultValue) {
        String val = config.getString("effects." + path + "." + key);
        if (val == null || val.isEmpty()) {
            if (path.equals("plus")) {
                val = config.getString("effects.kill." + key, defaultValue);
            } else if (path.equals("minus")) {
                val = config.getString("effects.death." + key, defaultValue);
            } else {
                val = defaultValue;
            }
        }
        return val == null ? defaultValue : val.trim();
    }

    public int getEffectParticleInt(String key, String path, int defaultValue) {
        if (!config.contains("effects." + path + "." + key)) {
            if (path.equals("plus")) {
                return config.getInt("effects.kill." + key, defaultValue);
            } else if (path.equals("minus")) {
                return config.getInt("effects.death." + key, defaultValue);
            }
        }
        return config.getInt("effects." + path + "." + key, defaultValue);
    }

    public static org.bukkit.Sound matchSound(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Sound name is null or empty");
        }
        String clean = name.trim().toLowerCase().replace("minecraft:", "");

        try {
            org.bukkit.Sound sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(clean));
            if (sound != null) return sound;
        } catch (Exception ignored) {}

        try {
            org.bukkit.Sound sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(clean.replace("_", ".")));
            if (sound != null) return sound;
        } catch (Exception ignored) {}

        try {
            org.bukkit.Sound sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(clean.replace(".", "_")));
            if (sound != null) return sound;
        } catch (Exception ignored) {}

        for (org.bukkit.Sound sound : org.bukkit.Registry.SOUNDS) {
            org.bukkit.NamespacedKey nsk = org.bukkit.Registry.SOUNDS.getKey(sound);
            if (nsk == null) continue;
            String key = nsk.getKey().toLowerCase();
            if (key.equals(clean) || key.replace("_", "").replace(".", "").equals(clean.replace("_", "").replace(".", ""))) {
                return sound;
            }
        }

        throw new IllegalArgumentException("Unknown sound: " + name);
    }
}
