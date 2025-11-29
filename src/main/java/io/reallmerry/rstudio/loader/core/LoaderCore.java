package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.managers.Downloader;
import io.reallmerry.rstudio.loader.managers.SetupState;
import io.reallmerry.rstudio.loader.managers.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;

public class LoaderCore {

    private final Loader plugin;
    private final LoggerManager log;
    private final ConfigManager config;
    private final SetupState setupState;
    private final WhitelistManager whitelistManager;

    public LoaderCore(Loader plugin, LoggerManager log, ConfigManager config, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.log = log;
        this.config = config;
        this.whitelistManager = whitelistManager;
        this.setupState = new SetupState(plugin, log);
    }

    public void start() throws Exception {
        if (setupState.isSetupCompleted()) {
            log.msg("<gray>Setup already completed. Skipping...");
            return;
        }

        boolean isFolia = checkIfFolia();

        if (isFolia) {
            // folia
            Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                runSetup();
            });
        } else {
            // paper
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::runSetup);
        }
    }

    private boolean checkIfFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void runSetup() {
        long totalStart = System.currentTimeMillis();

        long start = System.currentTimeMillis();
        setupWorld();
        long worldTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        setupWhitelist();
        long whitelistTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        downloadPlugins();
        long downloadTime = System.currentTimeMillis() - start;

        setupState.markSetupCompleted();

        long totalTime = System.currentTimeMillis() - totalStart;

        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            log.log("<gray>Loading time:");
            log.log("<dark_gray> ├ <gray>World: <white>" + worldTime + "ms");
            log.log("<dark_gray> ├ <gray>Whitelist: <white>" + whitelistTime + "ms");
            log.log("<dark_gray> ├ <gray>Plugins: <white>" + downloadTime + "ms");
            log.log("<dark_gray> └ <gray>Total: <white>" + totalTime + "ms");

            log.log("<green>The installer has been successfully downloaded and is ready to run!");

            log.log("<yellow>Server will restart in 5 seconds to complete setup...");

            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, restartTask -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
            }, 100L);
        });
    }

    private void setupWorld() {
        //support folia
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            World world = Bukkit.getWorlds().get(0);

            String difficultyStr = config.getConfig().getString("world.difficulty", "PEACEFUL");
            Difficulty difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
            world.setDifficulty(difficulty);

            boolean freezeTime = config.getConfig().getBoolean("world.freeze_time", true);
            int time = config.getConfig().getInt("world.time", 6000);
            world.setTime(time);

            if (freezeTime) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            }

            boolean disableWeather = config.getConfig().getBoolean("world.disable_weather", true);
            boolean disableThunder = config.getConfig().getBoolean("world.disable_thunder", true);

            if (disableWeather) {
                world.setStorm(false);
            }
            if (disableThunder) {
                world.setThundering(false);
            }

            log.log("<gray>World settings: " + difficulty + " difficulty, time=" + time +
                    (freezeTime ? ", frozen time" : "") +
                    (disableWeather ? ", frozen weather" : "") +
                    (disableThunder ? ", no thunder" : ""));
        });
    }

    private void setupWhitelist() {
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            boolean whitelistEnabled = config.getConfig().getBoolean("whitelist.enabled", true);
            Bukkit.setWhitelist(whitelistEnabled);

            if (whitelistEnabled) {
                String[] opPlayers = config.getOpPlayers();
                for (String playerName : opPlayers) {
                    Bukkit.getOfflinePlayer(playerName).setWhitelisted(true);
                    Bukkit.getOfflinePlayer(playerName).setOp(true);
                    log.log("<gray>Administrator rights have been granted to " + playerName + ".");
                }
            }

            log.log("<gray>The whitelist is " + (whitelistEnabled ? "activated" : "deactivated") + ".");
        });
    }

    private void downloadPlugins() {
        if (!config.isAutoDownloadEnabled()) {
            log.log("<gray>Auto-download disabled in configuration.");
            return;
        }

        String[] pluginUrls = config.getPluginUrls();
        if (pluginUrls.length == 0) {
            log.log("<gray>The list of plugins to download is empty.");
            return;
        }

        Downloader downloader = new Downloader(log);
        downloader.downloadPlugins(Arrays.asList(pluginUrls));
    }

    public SetupState getSetupState() {
        return this.setupState;
    }
}