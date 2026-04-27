package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.config.DownloadTask;
import io.reallmerry.rstudio.loader.config.PluginConfig;
import io.reallmerry.rstudio.loader.download.Http;
import io.reallmerry.rstudio.loader.download.PluginDownloader;
import io.reallmerry.rstudio.loader.notification.NotificationDispatcher;
import io.reallmerry.rstudio.loader.util.Platform;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LoaderCore {

    private final JavaPlugin plugin;
    private final PluginLogger log;
    private final PluginConfig config;
    private final Http http;
    private final SetupState setupState;

    public LoaderCore(JavaPlugin plugin, PluginLogger log, PluginConfig config, Http http) {
        this.plugin = plugin;
        this.log = log;
        this.config = config;
        this.http = http;
        this.setupState = new SetupState(plugin, log);
    }

    public void start() {
        if (setupState.completed()) {
            log.info("<gray>Setup already completed, skipping.");
            return;
        }
        Platform.scheduleAsync(plugin, this::runSetup);
    }

    private void runSetup() {
        long totalStart = System.currentTimeMillis();

        long worldStart = System.currentTimeMillis();
        Platform.scheduleOnMain(plugin, this::applyWorldSettings);
        long worldTime = System.currentTimeMillis() - worldStart;

        long whitelistStart = System.currentTimeMillis();
        Platform.scheduleOnMain(plugin, this::applyWhitelistSettings);
        long whitelistTime = System.currentTimeMillis() - whitelistStart;

        List<DownloadTask> tasks = config.plugins().list();
        long downloadTime = 0;

        if (config.plugins().autoDownload() && !tasks.isEmpty()) {
            var downloader = new PluginDownloader(log, http, config.plugins().backupRetainCount());
            downloadTime = downloader.downloadAll(tasks);
            new NotificationDispatcher(log, http, config.notifications(), plugin.getServer().getName())
                    .dispatch(tasks, downloadTime);
        } else if (!config.plugins().autoDownload()) {
            log.info("<gray>Auto-download is disabled.");
        } else {
            log.info("<gray>Plugin download list is empty.");
        }

        setupState.markCompleted();

        final long totalTime = System.currentTimeMillis() - totalStart;
        final long capturedDownload = downloadTime;

        Platform.scheduleOnMain(plugin, () -> {
            log.info("<gray>Setup timing:");
            log.info("<dark_gray> ├ <gray>World: <white>" + worldTime + "ms");
            log.info("<dark_gray> ├ <gray>Whitelist: <white>" + whitelistTime + "ms");
            log.info("<dark_gray> ├ <gray>Plugins: <white>" + capturedDownload + "ms");
            log.info("<dark_gray> └ <gray>Total: <white>" + totalTime + "ms");
            log.info("<green>Installer completed. Restarting in 5 seconds...");
            Platform.scheduleDelayedOnMain(
                    plugin,
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart"),
                    5 * 20L
            );
        });
    }

    private void applyWorldSettings() {
        World world = Bukkit.getWorlds().get(0);
        PluginConfig.WorldConfig wc = config.world();
        world.setDifficulty(Difficulty.valueOf(wc.difficulty().toUpperCase()));
        world.setTime(wc.time());
        if (wc.freezeTime()) world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        if (wc.disableWeather()) world.setStorm(false);
        if (wc.disableThunder()) world.setThundering(false);
    }

    private void applyWhitelistSettings() {
        PluginConfig.WhitelistConfig wc = config.whitelist();
        Bukkit.setWhitelist(wc.enabled());
        if (!wc.enabled()) return;
        for (String name : config.admin().opPlayers()) {
            var offline = Bukkit.getOfflinePlayer(name);
            offline.setWhitelisted(true);
            offline.setOp(true);
        }
    }
}