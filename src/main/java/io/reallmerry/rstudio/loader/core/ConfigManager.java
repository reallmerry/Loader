package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.utils.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final Loader plugin;
    private final LoggerManager log;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(Loader plugin, LoggerManager log) {
        this.plugin = plugin;
        this.log = log;
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.options().copyDefaults(true);
            config.save(configFile);
        } catch (IOException e) {
            log.log("<red>Could not save config to " + configFile);
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }

    public String[] getOpPlayers() {
        return getConfig().getStringList("admin.op_players").toArray(new String[0]);
    }

    public String[] getRejectionMessages() {
        return getConfig().getStringList("whitelist.rejection_messages").toArray(new String[0]);
    }

    public String[] getPluginUrls() {
        return getConfig().getStringList("plugins.download_urls").toArray(new String[0]);
    }

    public boolean isWhitelistEnabled() {
        return getConfig().getBoolean("whitelist.enabled", true);
    }

    public boolean isAutoDownloadEnabled() {
        return getConfig().getBoolean("plugins.auto_download", true);
    }
}