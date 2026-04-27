package io.reallmerry.rstudio.loader.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.reallmerry.rstudio.loader.config.PluginConfig;
import io.reallmerry.rstudio.loader.download.Http;
import io.reallmerry.rstudio.loader.util.Platform;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class UpdateChecker {

    private static final String RELEASES_URL =
            "https://api.github.com/repos/reallmerry/Loader/releases/latest";

    private final JavaPlugin plugin;
    private final PluginLogger log;
    private final PluginConfig config;
    private final Http http;

    public UpdateChecker(JavaPlugin plugin, PluginLogger log, PluginConfig config, Http http) {
        this.plugin = plugin;
        this.log = log;
        this.config = config;
        this.http = http;
    }

    public void schedule() {
        Platform.scheduleAsync(plugin, this::check);
    }

    private void check() {
        String userAgent = "RStudioLoader/" + plugin.getDescription().getVersion();
        try {
            HttpResponse<String> response = http.get(
                    RELEASES_URL,
                    Map.of("User-Agent", userAgent, "Accept", "application/vnd.github+json"),
                    Duration.ofSeconds(8)
            );
            if (response.statusCode() != 200) return;

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            String latest = json.get("tag_name").getAsString().replaceFirst("^v", "");
            String current = plugin.getDescription().getVersion().replaceFirst("^v", "");

            if (compareVersions(latest, current) > 0) notifyAdmins(current, latest);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.warn("<yellow>Update check failed: " + e.getMessage());
        }
    }

    private void notifyAdmins(String current, String latest) {
        if (!config.admin().notifyAboutUpdates()) return;
        var message = MiniMessage.miniMessage().deserialize(
                "<red><bold>rStudio: Update Available!</bold></red>\n" +
                        "<gray>Current: <white>v" + current + "\n" +
                        "<gray>Latest: <green>v" + latest + "\n" +
                        "<yellow>Download: <aqua><click:open_url:'https://github.com/reallmerry/Loader/releases/latest'>GitHub</click>"
        );
        log.info("<aqua>New version available: v" + latest + " (current: v" + current + ")");

        Platform.scheduleOnMain(plugin, () -> {
            var adminNames = config.admin().opPlayers();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp() && adminNames.contains(player.getName())) {
                    player.sendMessage(message);
                }
            }
        });
    }

    private int compareVersions(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int va = i < pa.length ? parseIntSafe(pa[i]) : 0;
            int vb = i < pb.length ? parseIntSafe(pb[i]) : 0;
            if (va != vb) return Integer.compare(va, vb);
        }
        return 0;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}