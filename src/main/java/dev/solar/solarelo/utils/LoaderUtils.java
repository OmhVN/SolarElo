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
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LoaderUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String INTEGRITY_ENTRY = "META-INF/solarelo/integrity.sha256";
    private static final String SEPARATOR =
            "=========================================================================";

    private final Logger logger;
    private final String productName;

    public LoaderUtils(Logger logger, String productName) {
        this.logger = logger;
        this.productName = productName;
    }

    public boolean check(Plugin plugin, java.io.File jarFile) {
        Path path = null;
        if (jarFile != null && jarFile.exists()) {
            path = jarFile.toPath();
        } else {
            path = resolveCurrentJarPath();
        }
        if (path == null) {
            return true;
        }
        boolean ok = verifyJar(path);
        if (!ok) {
            disable(plugin);
        }
        return ok;
    }

    public boolean checkPlugin(Plugin plugin) {
        if (!plugin.getDescription().getName().equals(productName) || !plugin.getDataFolder().getName().equals(productName)) {
            logger.severe("Invalid plugin or directory name!");
            disable(plugin);
            return false;
        }
        return true;
    }

    public boolean verifyJar(Path jarPath) {
        String expectedFingerprint = readExpectedFingerprint(jarPath);
        if (expectedFingerprint == null || expectedFingerprint.isBlank()) {
            logSuspiciousJar(
                    productName + " could not verify its embedded jar metadata.",
                    "This jar does not look like an original " + productName + " build.",
                    "The jar is missing its embedded integrity metadata.",
                    "This usually means the jar was rebuilt, unpacked, or modified."
            );
            return false;
        }

        try {
            String actualFingerprint = computeFingerprint(jarPath);
            if (actualFingerprint.equalsIgnoreCase(expectedFingerprint)) {
                logger.info(productName + " integrity verified (" + jarPath.getFileName() + ").");
                return true;
            }

            logSuspiciousJar(
                    productName + " detected that the jar was modified since build.",
                    "This is most likely malware or direct jar tampering."
            );
            return false;
        } catch (IOException | NoSuchAlgorithmException exception) {
            logSuspiciousJar(
                    productName + " could not complete its jar integrity check.",
                    "This usually means the jar is damaged, modified, or being interfered with.",
                    "Verification error: " + exception.getClass().getSimpleName()
            );
            return false;
        }
    }

    private Path resolveCurrentJarPath() {
        CodeSource codeSource = LoaderUtils.class.getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            return null;
        }
        try {
            Path path = Path.of(codeSource.getLocation().toURI()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path) || !path.getFileName().toString().endsWith(".jar")) {
                return null;
            }
            return path;
        } catch (URISyntaxException exception) {
            return null;
        }
    }

    private String readExpectedFingerprint(Path jarPath) {
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            ZipEntry entry = zipFile.getEntry(INTEGRITY_ENTRY);
            if (entry == null) {
                return null;
            }
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        } catch (IOException exception) {
            return null;
        }
    }

    private String computeFingerprint(Path jarPath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] buffer = new byte[8192];
        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            List<? extends ZipEntry> entries = zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> !Objects.equals(entry.getName(), INTEGRITY_ENTRY))
                    .sorted(Comparator.comparing(ZipEntry::getName))
                    .toList();

            for (ZipEntry entry : entries) {
                byte[] nameBytes = entry.getName().getBytes(StandardCharsets.UTF_8);
                digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(nameBytes.length).array());
                digest.update(nameBytes);
                digest.update(ByteBuffer.allocate(Long.BYTES).putLong(entry.getSize()).array());

                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void disable(Plugin plugin) {
        try {
            org.bukkit.Bukkit.getPluginManager().disablePlugin(plugin);
            org.bukkit.Bukkit.shutdown();
        } catch (Throwable ignored) {
            System.exit(0);
        }
    }

    private void logSuspiciousJar(String headline, String assessment, String... details) {
        logger.severe(SEPARATOR);
        logger.severe(" " + productName.toUpperCase() + " SECURITY ALERT ");
        logger.severe(SEPARATOR);
        logger.severe("");
        logger.severe(" " + headline);
        logger.severe(" " + assessment);

        for (String detail : details) {
            logger.severe(" " + detail);
        }

        logger.severe("");
        logger.severe(" Required action:");
        logger.severe(" 1. Delete this " + productName + " jar.");
        logger.severe(" 2. Reinstall " + productName + " from a trusted source.");
        logger.severe(" 3. If this warning appears again after reinstalling");
        logger.severe("    " + productName + ", malware is most likely modifying");
        logger.severe("    plugin jars or the server jar during startup.");
        logger.severe(" 4. Reinstall your server jar and every plugin");
        logger.severe("    from trusted sources.");
        logger.severe("");
        logger.severe(" " + productName + " has disabled itself.");
        logger.severe(SEPARATOR);
    }
}
