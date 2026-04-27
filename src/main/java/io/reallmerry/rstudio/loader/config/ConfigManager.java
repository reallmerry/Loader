package io.reallmerry.rstudio.loader.config;

import io.reallmerry.rstudio.loader.core.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private final PluginLogger log;

    private PluginConfig pluginConfig;
    private YamlConfigurationLoader loader;

    public ConfigManager(JavaPlugin plugin, PluginLogger log) {
        this.plugin = plugin;
        this.log = log;
    }

    public boolean load() {
        Path configPath = plugin.getDataFolder().toPath().resolve("config.yml");

        try {
            if (!Files.exists(configPath)) {
                plugin.getDataFolder().mkdirs();
                plugin.saveResource("config.yml", false);
            }
        } catch (Exception e) {
            log.error("<red>Failed to extract default config: " + e.getMessage());
            return false;
        }

        this.loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .nodeStyle(NodeStyle.BLOCK)
                .defaultOptions(opts -> opts.serializers(build -> build
                        .register(DownloadTask.class, DownloadTaskSerializer.INSTANCE)
                        .registerAnnotatedObjects(
                                org.spongepowered.configurate.objectmapping.ObjectMapper.factoryBuilder().build()
                        )
                ))
                .build();

        return reload();
    }

    public boolean reload() {
        try {
            CommentedConfigurationNode node = loader.load();
            this.pluginConfig = node.get(PluginConfig.class);
            if (this.pluginConfig == null) {
                this.pluginConfig = new PluginConfig();
            }
            return true;
        } catch (ConfigurateException e) {
            log.error("<red>Failed to load config.yml: " + e.getMessage());
            return false;
        }
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }
}
