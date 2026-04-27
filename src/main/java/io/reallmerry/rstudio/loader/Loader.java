package io.reallmerry.rstudio.loader;

import io.reallmerry.rstudio.loader.config.ConfigManager;
import io.reallmerry.rstudio.loader.core.LoaderCore;
import io.reallmerry.rstudio.loader.core.PluginLogger;
import io.reallmerry.rstudio.loader.core.UpdateChecker;
import io.reallmerry.rstudio.loader.download.Http;
import io.reallmerry.rstudio.loader.listener.PlayerAccess;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class Loader extends JavaPlugin {

    private PluginLogger log;
    private LoaderCore core;

    @Override
    public void onEnable() {
        this.log = new PluginLogger(getComponentLogger());
        var config = new ConfigManager(this, log);

        if (!config.load()) {
            log.error("<red>Failed to load configuration. Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        var pluginConfig = config.pluginConfig();
        var http = new Http(Duration.ofSeconds(10));

        getServer().getPluginManager().registerEvents(
                new PlayerAccess(pluginConfig, log), this
        );

        new UpdateChecker(this, log, pluginConfig, http).schedule();

        this.core = new LoaderCore(this, log, pluginConfig, http);
        core.start();
    }

    @Override
    public void onDisable() {
        log.info("<gray>Stopping installer...");
    }
}