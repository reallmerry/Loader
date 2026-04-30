package io.reallmerry.rstudio.loader.download;

import io.reallmerry.rstudio.loader.config.DownloadTask;
import io.reallmerry.rstudio.loader.core.PluginLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

public final class PluginDownloader implements Downloader {

    private final PluginLogger log;
    private final Http http;
    private final int backupRetainCount;

    public PluginDownloader(PluginLogger log, Http http, int backupRetainCount) {
        this.log = log;
        this.http = http;
        this.backupRetainCount = backupRetainCount;
    }

    @Override
    public long downloadAll(List<DownloadTask> tasks) {
        long start = System.currentTimeMillis();
        for (DownloadTask task : tasks) {
            try {
                download(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("<red>Download interrupted for: " + task.url());
                break;
            } catch (Exception e) {
                log.error("<red>Failed to download " + task.fileName() + ": " + e.getMessage());
            }
        }
        return System.currentTimeMillis() - start;
    }

    private void download(DownloadTask task) throws IOException, InterruptedException {
        Files.createDirectories(Path.of("plugins"));
        log.info("<gray>Downloading <white>" + task.fileName() + "<gray>...");

        HttpResponse<InputStream> response = http.download(
                task.url(), task.headers(), Duration.ofMinutes(5)
        );
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for " + task.url());
        }

        Path destination = Path.of("plugins", task.fileName());
        Path tempFile = Path.of("plugins", task.fileName() + ".tmp");

        try (InputStream body = response.body()) {
            Files.copy(body, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        if (task.hasValidHash()) verifySha256(tempFile, task.sha256(), task.fileName());
        if (Files.exists(destination)) backup(destination, task.fileName());
        Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);

        log.info("<green>Downloaded <white>" + task.fileName());
    }

    private void verifySha256(Path file, String expected, String fileName) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Files.readAllBytes(file));
            String actual = HexFormat.of().formatHex(digest.digest());
            if (!actual.equalsIgnoreCase(expected)) {
                Files.deleteIfExists(file);
                throw new IOException(
                        "SHA-256 mismatch for " + fileName + ": expected=" + expected + ", got=" + actual
                );
            }
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private void backup(Path existing, String pluginName) throws IOException {
        String baseName = pluginName.replace(".jar", "");
        Path backupDir = Path.of("plugins", "backups", baseName);
        Files.createDirectories(backupDir);
        Files.copy(
                existing,
                backupDir.resolve(Instant.now().toEpochMilli() + ".jar"),
                StandardCopyOption.REPLACE_EXISTING
        );
        pruneBackups(backupDir);
    }

    private void pruneBackups(Path backupDir) throws IOException {
        try (Stream<Path> files = Files.list(backupDir)) {
            List<Path> sorted = files
                    .filter(p -> p.toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
            if (sorted.size() > backupRetainCount) {
                for (Path old : sorted.subList(0, sorted.size() - backupRetainCount)) {
                    Files.deleteIfExists(old);
                }
            }
        }
    }
}