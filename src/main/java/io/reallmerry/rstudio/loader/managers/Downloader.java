package io.reallmerry.rstudio.loader.managers;

import io.reallmerry.rstudio.loader.core.LoggerManager;
import io.reallmerry.rstudio.loader.Loader;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Downloader {

    private final LoggerManager log;
    private final ExecutorService executor;
    private final Loader plugin;
    private final HttpClient httpClient;

    public Downloader(LoggerManager log) {
        this.log = log;
        this.executor = Executors.newFixedThreadPool(3);
        this.plugin = (Loader) Bukkit.getPluginManager().getPlugin("Loader");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void downloadPlugins(List<String> urls) {
        if (urls.isEmpty()) {
            log.msg("<gray>The list of plugins to download is empty.");
            return;
        }

        log.msg("<gray>Starting plugin download process (" + urls.size() + " plugins)...");

        CompletableFuture<Void>[] futures = new CompletableFuture[urls.size()];
        int index = 0;

        for (String urlString : urls) {
            urlString = urlString.trim();
            if (urlString.isEmpty()) continue;

            final String finalUrlString = urlString;
            futures[index] = CompletableFuture.runAsync(() -> downloadPlugin(finalUrlString), executor);
            index++;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        CompletableFuture.allOf(futures).join();

        log.msg("<green>All plugins downloaded successfully!");

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void downloadPlugin(String urlString) {
        try {
            URI uri = new URI(urlString);
            String fileName = Paths.get(uri.getPath()).getFileName().toString();
            Path pluginsDir = Paths.get("plugins");
            Path destination = pluginsDir.resolve(fileName);

            if (!Files.exists(pluginsDir)) {
                Files.createDirectories(pluginsDir);
            }

            if (plugin != null) {
                Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask ->
                        log.msg("<gray>Downloading <white>" + fileName + " <gray>..."));
            } else {
                log.msg("<gray>Downloading <white>" + fileName + " <gray>...");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMinutes(2))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                try (InputStream inputStream = response.body()) {
                    Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
                }


            } else {
                if (plugin != null) {
                    Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask ->
                            log.error("<red>Error during installation " + urlString + ": HTTP " + response.statusCode()));
                } else {
                    log.error("<red>Error during installation " + urlString + ": HTTP " + response.statusCode());
                }
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            if (plugin != null) {
                Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask ->
                        log.error("<red>Error during installation " + urlString + ": " + e.getMessage()));
            } else {
                log.error("<red>Error during installation " + urlString + ": " + e.getMessage());
            }

            log.error("<red>Full error: " + e.toString());
        }
    }
}