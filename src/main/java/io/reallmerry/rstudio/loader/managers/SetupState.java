package io.reallmerry.rstudio.loader.managers;

import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.core.LoggerManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class SetupState {

    private final Loader plugin;
    private final File stateFile;
    private final LoggerManager log;
    private final YamlConfiguration config;

    public SetupState(Loader plugin, LoggerManager log) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "setup-done.yml");
        this.config = YamlConfiguration.loadConfiguration(stateFile);
        this.log = log;

        if (!stateFile.exists()) {
            try {
                stateFile.getParentFile().mkdirs();
                config.set("setup_completed", false);
                config.save(stateFile);
            } catch (IOException e) {
                log.msg("Failed to create setup state file: " + e.getMessage());
            }
        }
    }

    public boolean isSetupCompleted() {
        return config.getBoolean("setup_completed", false);
    }

    public void markSetupCompleted() {
        config.set("setup_completed", true);

        try {
            config.save(stateFile);
        } catch (IOException e) {
            log.msg("Failed to save the settings status: " + e.getMessage());
        }
    }
}