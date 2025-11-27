package io.reallmerry.rstudio.loader;

import io.reallmerry.rstudio.loader.core.LoaderCore;
import io.reallmerry.rstudio.loader.core.LoggerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Loader extends JavaPlugin {

    private LoggerManager log;

    @Override
    public void onEnable() {
        log = new LoggerManager(this, "&crStudio &7| ");
        log.log("<gray>Starting the test server...");
        log.log("<gray>Running the installer on x64 architecture...");


        try {
            LoaderCore core = new LoaderCore(this, log);
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