package dev.solar.solarelo.utils;

import org.bukkit.plugin.Plugin;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LoaderUtils {

    private static final String HASH_ALGO = "SHA-256";
    private static final String INTEGRITY_PATH = dec(new byte[]{
        23,31,14,27,119,19,20,28,117,55,59,44,63,52,117,62,63,44,116,41,53,54,59,40,117,41,53,54,59,40,63,54,53,117,42,53,55,116,42,40,53,42,63,40,46,51,63,41
    });
    private static final String VERSION_KEY = dec(new byte[]{
        40, 63, 44, 51, 41, 51, 53, 52
    });
    private static final String EXPECTED_NAME = dec(new byte[]{
        9, 53, 54, 59, 40, 31, 54, 53
    });

    private static String dec(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (byte) (bytes[i] ^ 0x5A);
        }
        return new String(result, StandardCharsets.UTF_8);
    }

    private final Logger logger;
    private final String name;

    public LoaderUtils(Logger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public boolean check(Plugin plugin, java.io.File jarFile) {
        Path path = null;
        if (jarFile != null && jarFile.exists()) {
            path = jarFile.toPath();
        } else {
            path = getJarPath();
        }
        if (path == null) {
            return true;
        }
        boolean ok = verify(path);
        if (!ok) {
            disable(plugin);
        }
        return ok;
    }

    public boolean checkPlugin(Plugin plugin) {
        if (!plugin.getDescription().getName().equals(EXPECTED_NAME) || !plugin.getDataFolder().getName().equals(EXPECTED_NAME)) {
            logger.severe("\u001b[31m\u001b[1mSECURITY\u001b[0m >> Invalid plugin or directory name! Plugin will be disabled.");
            disable(plugin);
            return false;
        }
        return true;
    }

    private void disable(Plugin plugin) {
        try {
            String mGetServer = dec(new byte[]{61, 63, 46, 9, 63, 40, 44, 63, 40});
            Object server = plugin.getClass().getMethod(mGetServer).invoke(plugin);
            
            String mGetPluginManager = dec(new byte[]{61, 63, 46, 10, 54, 47, 61, 51, 52, 23, 59, 52, 59, 61, 63, 40});
            Object pluginManager = server.getClass().getMethod(mGetPluginManager).invoke(server);
            
            String mDisablePlugin = dec(new byte[]{62, 51, 41, 59, 56, 54, 63, 10, 54, 47, 61, 51, 52});
            pluginManager.getClass().getMethod(mDisablePlugin, Plugin.class).invoke(pluginManager, plugin);
            
            String mShutdown = dec(new byte[]{41, 50, 47, 46, 62, 53, 45, 52});
            server.getClass().getMethod(mShutdown).invoke(server);
        } catch (Throwable ignored) {
            try {
                org.bukkit.Bukkit.getPluginManager().disablePlugin(plugin);
                org.bukkit.Bukkit.shutdown();
            } catch (Throwable ignored2) {
                System.exit(0);
            }
        }
    }

    public boolean verify(Path path) {
        String expected = getExpectedFingerprint(path);
        if (expected == null || expected.isBlank()) {
            logAlert("Plugin verification failed.", "Embedded signature is missing.", "Please build the plugin using the proper Gradle task.");
            return false;
        }

        try {
            String actual = getActualFingerprint(path);
            if (actual.equalsIgnoreCase(expected)) {
                logger.info("Integrity check passed.");
                return true;
            }
            logAlert("Plugin file was modified.", "This jar appears to be tampered with.", "Please restore the original unmodified jar file.");
            return false;
        } catch (IOException | NoSuchAlgorithmException e) {
            logAlert("Verification error.", "Could not complete integrity check.", "Error: " + e.getClass().getSimpleName());
            return false;
        }
    }

    private Path getJarPath() {
        CodeSource source = LoaderUtils.class.getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) {
            return null;
        }
        try {
            Path path = Path.of(source.getLocation().toURI()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path) || !path.getFileName().toString().endsWith(".jar")) {
                return null;
            }
            return path;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private String getExpectedFingerprint(Path path) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            ZipEntry entry = zip.getEntry(INTEGRITY_PATH);
            if (entry == null) {
                return null;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                Properties props = new Properties();
                props.load(in);
                return props.getProperty(VERSION_KEY);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private String getActualFingerprint(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
        byte[] buffer = new byte[8192];
        try (ZipFile zip = new ZipFile(path.toFile())) {
            List<? extends ZipEntry> entries = zip.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> !Objects.equals(entry.getName(), INTEGRITY_PATH))
                    .sorted(Comparator.comparing(ZipEntry::getName))
                    .toList();
            for (ZipEntry entry : entries) {
                byte[] nameBytes = entry.getName().getBytes(StandardCharsets.UTF_8);
                digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(nameBytes.length).array());
                digest.update(nameBytes);
                digest.update(ByteBuffer.allocate(Long.BYTES).putLong(entry.getSize()).array());
                try (InputStream in = zip.getInputStream(entry)) {
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void logAlert(String title, String desc, String action) {
        String line = "======================================================================";
        logger.severe(line);
        logger.severe(" " + name.toUpperCase() + " SECURITY CHECK ");
        logger.severe(line);
        logger.severe(" " + title);
        logger.severe(" " + desc);
        logger.severe("");
        logger.severe(" Action required:");
        logger.severe(" 1. " + action);
        logger.severe(" 2. Reinstall " + name + " from a official resource.");
        logger.severe(line);
    }
}
