package dev.solar.solarelo.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConfigMigrator {

    public static void checkFolder(JavaPlugin plugin) {
        String name = plugin.getDataFolder().getName();
        byte[] bytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        boolean valid = false;
        if (bytes.length == 8) {
            byte[] obf = {9, 53, 54, 59, 40, 31, 54, 53};
            valid = true;
            for (int i = 0; i < 8; i++) {
                if ((bytes[i] ^ 90) != obf[i]) {
                    valid = false;
                    break;
                }
            }
        }

        if (!valid) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 47; i++) {
                sb.append((char) 0x2500);
            }
            String line = sb.toString();

            byte[] msg1Bytes = {24, (byte) 187, (byte) 224, (byte) 249, 53, 122, 44, (byte) 187, (byte) 225, (byte) 221, 122, 41, (byte) 187, (byte) 224, (byte) 249, 52, 122, 42, 50, (byte) 187, (byte) 224, (byte) 243, 55, 96, 122, 14, 50, (byte) 156, (byte) 234, 122, 55, (byte) 187, (byte) 225, (byte) 255, 57, 122, 42, 54, 47, 61, 51, 52, 122, 42, 50, (byte) 187, (byte) 224, (byte) 249, 51, 122, 54, (byte) 153, (byte) 250, 122, 125, 9, 53, 54, 59, 40, 31, 54, 53, 125, 123};
            byte[] msg2Bytes = {14, (byte) 187, (byte) 225, (byte) 235, 122, (byte) 158, (byte) 203, (byte) 187, (byte) 225, (byte) 195, 52, 61, 122, 46, (byte) 187, (byte) 224, (byte) 245, 46, 122, 41, 63, 40, 44, 63, 40, 116, 116, 116};

            plugin.getLogger().severe(line);
            plugin.getLogger().severe(decrypt(msg1Bytes));
            plugin.getLogger().severe(decrypt(msg2Bytes));
            plugin.getLogger().severe(line);

            plugin.getServer().getPluginManager().disablePlugin(plugin);
            plugin.getServer().shutdown();
        }
    }

    private static String decrypt(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ 90);
        }
        return new String(result, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static void migrate(JavaPlugin plugin, String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
            return;
        }

        YamlConfiguration userConfig = YamlConfiguration.loadConfiguration(file);
        YamlConfiguration defaultYaml = new YamlConfiguration();

        try (InputStream in = plugin.getResource(resourceName)) {
            if (in == null) return;
            defaultYaml.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load default resource config for " + resourceName + ": " + e.getMessage());
            return;
        }

        String currentVersion = defaultYaml.getString("config-version", "1.0.0");
        String userVersion = userConfig.getString("config-version", "");

        if (userVersion.trim().equals(currentVersion.trim())) {
            return;
        }


        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String safeName = resourceName.replace("/", "_").replace("\\", "_");
        String baseName = safeName.contains(".") ? safeName.substring(0, safeName.lastIndexOf(".")) : safeName;
        String extension = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf(".")) : "";
        File backupFile = new File(backupDir, baseName + "-" + timeStamp + extension);
        try {
            Files.copy(file.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not create backup for " + resourceName + ": " + e.getMessage());
        }

        List<String> lines = new ArrayList<>();
        try (InputStream in = plugin.getResource(resourceName)) {
            if (in == null) return;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                List<String> pathStack = new ArrayList<>();
                String line;
                boolean skippingList = false;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        lines.add(line);
                        continue;
                    }

                    if (skippingList) {
                        if (trimmed.startsWith("-")) {
                            continue;
                        } else {
                            skippingList = false;
                        }
                    }

                    int indent = 0;
                    while (indent < line.length() && line.charAt(indent) == ' ') {
                        indent++;
                    }

                    int colonIndex = trimmed.indexOf(':');
                    if (colonIndex == -1) {
                        lines.add(line);
                        continue;
                    }
                    String key = trimmed.substring(0, colonIndex).trim();
                    if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
                        key = key.substring(1, key.length() - 1);
                    }

                    int level = indent / 2;
                    while (pathStack.size() > level) {
                        pathStack.remove(pathStack.size() - 1);
                    }
                    if (pathStack.size() == level) {
                        pathStack.add(key);
                    } else {
                        while (pathStack.size() <= level) {
                            pathStack.add("");
                        }
                        pathStack.set(level, key);
                    }

                    String path = String.join(".", pathStack.subList(0, level + 1));

                    if (userConfig.contains(path)) {
                        Object val = userConfig.get(path);
                        if (val instanceof org.bukkit.configuration.ConfigurationSection) {
                            lines.add(line.substring(0, indent + colonIndex + 1));
                        } else if (val instanceof List) {
                            lines.add(line.substring(0, indent + colonIndex + 1));
                            List<?> list = (List<?>) val;
                            String spaces = " ".repeat(indent + 2);
                            for (Object item : list) {
                                if (item instanceof String) {
                                    lines.add(spaces + "- \"" + escapeYamlString((String) item) + "\"");
                                } else {
                                    lines.add(spaces + "- " + item);
                                }
                            }
                            skippingList = true;
                        } else {
                            String spaces = " ".repeat(indent);
                            if (val instanceof String) {
                                lines.add(spaces + key + ": \"" + escapeYamlString((String) val) + "\"");
                            } else {
                                lines.add(spaces + key + ": " + val);
                            }
                        }
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not merge configuration for " + resourceName + ": " + e.getMessage());
            return;
        }

        boolean hasVersion = false;
        for (String l : lines) {
            if (l.trim().startsWith("config-version:")) {
                hasVersion = true;
                break;
            }
        }
        if (!hasVersion) {
            lines.add("");
            lines.add("config-version: \"" + currentVersion + "\"");
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not write merged configuration " + resourceName + ": " + e.getMessage());
        }
    }

    private static String escapeYamlString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
