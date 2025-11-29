package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.Loader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private final Loader plugin;
    private final String githubRepo = "reallmerry/Loader";
    private String latestVersion;
    private boolean hasUpdate;
    private final LoggerManager log;

    public UpdateChecker(Loader plugin) {
        this.plugin = plugin;
        this.log = new LoggerManager(plugin, "&crStudio &7| ");
    }

    public void checkForUpdates() {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + githubRepo + "/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "RStudioLoader/" + plugin.getDescription().getVersion());
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                        StringBuilder response = new StringBuilder();
                        char[] buffer = new char[1024];
                        int charsRead;
                        while ((charsRead = reader.read(buffer)) != -1) {
                            response.append(buffer, 0, charsRead);
                        }

                        String jsonResponse = response.toString();
                        int versionStart = jsonResponse.indexOf("\"tag_name\":\"v") + 12;
                        int versionEnd = jsonResponse.indexOf("\"", versionStart);

                        if (versionStart > 12 && versionEnd > versionStart) {
                            this.latestVersion = jsonResponse.substring(versionStart, versionEnd);
                            String currentVersion = plugin.getDescription().getVersion();

                            String latestVersionClean = this.latestVersion.startsWith("v") ? this.latestVersion.substring(1) : this.latestVersion;
                            String currentVersionClean = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

                            this.hasUpdate = compareVersions(latestVersionClean, currentVersionClean) > 0;

                            if (hasUpdate) {
                                Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> notifyAdmins());
                            }
                        }
                    }
                }
                connection.disconnect();
            } catch (IOException e) {
                log.error("Failed to check for updates: " + e.getMessage());
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

    private void notifyAdmins() {
        if (!plugin.getConfig().getBoolean("admin.notify_about_updates", true)) {
            return;
        }

        String currentVersion = plugin.getDescription().getVersion();
        String message = ChatColor.translateAlternateColorCodes('&',
                "&c&lRStudio Loader Update Available!\n" +
                        "&7Current version: &fv" + currentVersion + "\n" +
                        "&7Latest version: &av" + latestVersion + "\n" +
                        "&eDownload: &bhttps://github.com/reallmerry/Loader/releases/latest");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() && plugin.getConfig().getStringList("admin.op_players").contains(player.getName())) {
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