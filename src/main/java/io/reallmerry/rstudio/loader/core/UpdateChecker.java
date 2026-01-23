package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class UpdateChecker {

    private final Loader plugin;
    private final LoggerManager log;
    private final ConfigManager config;
    private final HttpClient client;
    private String latestVersion;
    private boolean hasUpdate;
    private static final String UPDATE_CHECK_URL = "https://api.github.com/repos/reallmerry/Loader/releases/latest";

    public UpdateChecker(Loader plugin, LoggerManager log, ConfigManager config) {
        this.plugin = plugin;
        this.log = log;
        this.config = config;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public void checkForUpdates() {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(UPDATE_CHECK_URL))
                        .timeout(Duration.ofSeconds(5))
                        .header("User-Agent", "RStudioLoader/" + plugin.getDescription().getVersion())
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    int tagStart = responseBody.indexOf("\"tag_name\":\"v") + 12;
                    int tagEnd = responseBody.indexOf("\"", tagStart);

                    if (tagStart > 12 && tagEnd > tagStart) {
                        this.latestVersion = responseBody.substring(tagStart, tagEnd);
                        String currentVersion = plugin.getDescription().getVersion();
                        String latestClean = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
                        String currentClean = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

                        this.hasUpdate = compareVersions(latestClean, currentClean) > 0;

                        if (hasUpdate) {
                            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> notifyAdmins(currentVersion));
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.error("Failed to check for updates: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = i < parts1.length ? parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? parseInt(parts2[i]) : 0;

            if (part1 > part2) {
                return 1;
            } else if (part1 < part2) {
                return -1;
            }
        }
        return 0;
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void notifyAdmins(String currentVersion) {
        if (!config.getConfig().getBoolean("admin.notify_about_updates", true)) {
            return;
        }

        List<String> adminNames = config.getConfig().getStringList("admin.op_players");

        String message = ColorUtil.parseColor(
                "<red><bold>rStudio: Loader Update Available!</red></bold>\n" +
                        "<gray>Current version: <white>v" + currentVersion + "\n" +
                        "<gray>Latest version: <green>v" + latestVersion + "\n" +
                        "<yellow>Download: <aqua>https://github.com/reallmerry/Loader/releases/latest"
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() && adminNames.contains(player.getName())) {
                player.sendMessage(message);
            }
        }

        log.log("New version available: v" + latestVersion + " (current: v" + currentVersion + ")");
    }

    public boolean hasUpdate() {
        return hasUpdate;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}