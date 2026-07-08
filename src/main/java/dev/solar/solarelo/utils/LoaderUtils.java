package dev.solar.solarelo.utils;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LoaderUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String INTEGRITY_ENTRY = "META-INF/solarelo/integrity.sha256";
    private static final String SEPARATOR =
            "=========================================================================";

    public static void checkStatic(String productName) {
        Path path = findOriginalJar(productName);
        if (path == null) {
            path = resolveCurrentJarPath();
        }
        if (path == null) {
            return;
        }

        boolean ok = verifyJarWithRetry(productName, path);
        if (!ok) {
            throw new SecurityException("[" + productName + "] Jar file integrity verification failed! The plugin has been disabled to prevent potential malware execution.");
        }
    }

    private static void waitForFileCopy(Path path) {
        java.io.File file = path.toFile();
        long lastSize = -1;
        for (int i = 0; i < 20; i++) {
            long currentSize = file.length();
            if (currentSize > 0 && currentSize == lastSize) {
                break;
            }
            lastSize = currentSize;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static boolean verifyJarWithRetry(String productName, Path jarPath) {
        waitForFileCopy(jarPath);
        String expectedFingerprint = null;
        
        for (int i = 0; i < 5; i++) {
            expectedFingerprint = readExpectedFingerprint(jarPath);
            if (expectedFingerprint != null && !expectedFingerprint.isBlank()) {
                break;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        if (expectedFingerprint == null || expectedFingerprint.isBlank()) {
            logSuspiciousJar(
                    productName,
                    productName + " could not verify its embedded jar metadata.",
                    "This jar does not look like an original " + productName + " build.",
                    "The jar is missing its embedded integrity metadata.",
                    "This usually means the jar was rebuilt, unpacked, or modified."
            );
            return false;
        }

        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                String actualFingerprint = computeFingerprint(jarPath);
                if (actualFingerprint.equalsIgnoreCase(expectedFingerprint)) {
                    System.out.println("[" + productName + "] Integrity verified (" + jarPath.getFileName() + ").");
                    return true;
                }
            } catch (IOException | NoSuchAlgorithmException ignored) {
                
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        logSuspiciousJar(
                productName,
                productName + " detected that the jar was modified since build.",
                "This is most likely malware or direct jar tampering."
        );
        return false;
    }

    private static Path findOriginalJar(String productName) {
        Path currentPath = resolveCurrentJarPath();
        if (currentPath != null) {
            String fileName = currentPath.getFileName().toString();
            java.io.File originalFile = new java.io.File("plugins", fileName);
            if (originalFile.exists()) {
                return originalFile.toPath();
            }
        }

        java.io.File pluginsDir = new java.io.File("plugins");
        if (!pluginsDir.isDirectory()) {
            return null;
        }
        java.io.File[] files = pluginsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (java.io.File file : files) {
                try (ZipFile zipFile = new ZipFile(file)) {
                    ZipEntry entry = zipFile.getEntry("plugin.yml");
                    if (entry != null) {
                        try (InputStream in = zipFile.getInputStream(entry)) {
                            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                if (line.startsWith("name:")) {
                                    String nameVal = line.substring(5).trim().replace("'", "").replace("\"", "");
                                    if (nameVal.equals(productName)) {
                                        return file.toPath();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private static Path resolveCurrentJarPath() {
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

    private static String readExpectedFingerprint(Path jarPath) {
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

    private static String computeFingerprint(Path jarPath) throws IOException, NoSuchAlgorithmException {
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

    private static void logSuspiciousJar(String productName, String headline, String assessment, String... details) {
        System.err.println(SEPARATOR);
        System.err.println(" " + productName.toUpperCase() + " SECURITY ALERT ");
        System.err.println(SEPARATOR);
        System.err.println("");
        System.err.println(" " + headline);
        System.err.println(" " + assessment);

        for (String detail : details) {
            System.err.println(" " + detail);
        }

        System.err.println("");
        System.err.println(" Required action:");
        System.err.println(" 1. Delete this " + productName + " jar.");
        System.err.println(" 2. Reinstall " + productName + " from a trusted source.");
        System.err.println(" 3. If this warning appears again after reinstalling");
        System.err.println("    " + productName + ", malware is most likely modifying");
        System.err.println("    plugin jars or the server jar during startup.");
        System.err.println(" 4. Reinstall your server jar and every plugin");
        System.err.println("    from trusted sources.");
        System.err.println("");
        System.err.println(" " + productName + " has disabled itself.");
        System.err.println(SEPARATOR);
    }
}
