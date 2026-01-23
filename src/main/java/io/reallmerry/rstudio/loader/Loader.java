package io.reallmerry.rstudio.loader;

import io.reallmerry.rstudio.loader.core.ConfigManager;
import io.reallmerry.rstudio.loader.core.LoggerManager;
import io.reallmerry.rstudio.loader.core.LoaderCore;
import io.reallmerry.rstudio.loader.core.UpdateChecker;
import io.reallmerry.rstudio.loader.managers.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Loader extends JavaPlugin {

    private LoggerManager log;
    private ConfigManager config;
    private LoaderCore core;
    private WhitelistManager whitelistManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        this.log = new LoggerManager(this, "&crStudio &7| ");

        this.config = new ConfigManager(this, log);
        this.config.loadConfig();

        this.updateChecker = new UpdateChecker(this, log, config);
        this.updateChecker.checkForUpdates();

        log.log("<gray>Starting the test server...");
        log.log("<gray>Running the installer on x64 architecture...");

        try {
            this.whitelistManager = new WhitelistManager(this, log, config);
            this.core = new LoaderCore(this, log, config, whitelistManager);
            core.start();

        } catch (Exception e) {
            log.error("<red>Error during loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        log.log("<gray>Stopping the test server and installer...");
    }
}