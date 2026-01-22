package io.reallmerry.rstudio.loader.managers;

import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.core.LoggerManager;
import org.bukkit.Bukkit;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {

    private final Loader plugin;
    private final LoggerManager log;
    private final ExecutorService executor;
    private final HttpClient httpClient;
    private final Path pluginsDir;

    public Downloader(Loader plugin, LoggerManager log) {
        this.plugin = plugin;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(3);
        this.pluginsDir = Paths.get("plugins");

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void downloadPlugins(List<String> urls) {
        List<String> validUrls = urls.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (validUrls.isEmpty()) {
            log.warn("<gray>No plugins to download.");
            return;
        }

        log.log("<gray>Starting download of <white>" + validUrls.size() + " <gray>plugins...");

        try {
            Files.createDirectories(pluginsDir);
        } catch (Exception e) {
            log.error("<red>Cannot create plugins directory: " + e.getMessage());
            return;
        }

        for (String url : validUrls) {
            executor.execute(() -> downloadSingle(url));
        }

        executor.shutdown();
    }

    private void downloadSingle(String urlString) {
        try {
            URI uri = URI.create(urlString);
            String fileName = Paths.get(uri.getPath()).getFileName().toString();
            Path destination = pluginsDir.resolve(fileName);

            runSync(() ->
                    log.msg("<gray>Downloading <white>" + fileName + "<gray>...")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMinutes(2))
                    .GET()
                    .build();

            HttpResponse<InputStream> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                runSync(() ->
                        log.error("<red>Failed to download " + fileName + ": HTTP " + response.statusCode())
                );
                return;
            }

            try (InputStream in = response.body()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            runSync(() ->
                    log.msg("<green>Downloaded <white>" + fileName)
            );

        } catch (Exception e) {
            runSync(() ->
                    log.error("<red>Download error (" + urlString + "): " + e.getMessage())
            );
        }
    }

    private void runSync(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
}
