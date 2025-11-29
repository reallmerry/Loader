package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.Loader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    private final Loader plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(Loader plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    public boolean isWhitelistEnabled() {
        return config.getBoolean("whitelist.enabled", true);
    }

    public boolean isAutoDownloadEnabled() {
        return config.getBoolean("plugins.auto_download", true);
    }

    public String[] getOpPlayers() {
        return config.getStringList("admin.op_players").toArray(new String[0]);
    }

    public String[] getRejectionMessages() {
        return config.getStringList("whitelist.rejection_messages").toArray(new String[0]);
    }

    public String[] getPluginUrls() {
        return config.getStringList("plugins.download_urls").toArray(new String[0]);
    }
}