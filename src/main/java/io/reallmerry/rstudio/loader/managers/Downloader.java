package io.reallmerry.rstudio.loader.managers;

import io.reallmerry.rstudio.loader.core.LoggerManager;
import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.core.ConfigManager;
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

public class Downloader {

    private final LoggerManager log;
    private final ConfigManager config;
    private final HttpClient httpClient;

    public Downloader(LoggerManager log, ConfigManager config) {
        this.log = log;
        this.config = config;
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

        for (String urlString : urls) {
            urlString = urlString.trim();
            if (urlString.isEmpty()) continue;

            downloadPlugin(urlString);
        }

        log.msg("<green>All plugins downloaded successfully!");
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

            log.msg("<gray>Downloading <white>" + fileName + " <gray>...");

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
                log.error("<red>Error during installation " + urlString + ": HTTP " + response.statusCode());
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            log.error("<red>Error during installation " + urlString + ": " + e.getMessage());
            log.error("<red>Full error: " + e.toString());
            Thread.currentThread().interrupt();
        }
    }
}