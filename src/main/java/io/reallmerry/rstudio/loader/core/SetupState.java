package io.reallmerry.rstudio.loader.core;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;

public final class SetupState {

    private final PluginLogger log;
    private final YamlConfigurationLoader loader;
    private ConfigurationNode cachedNode;

    public SetupState(JavaPlugin plugin, PluginLogger log) {
        this.log = log;
        Path statePath = plugin.getDataFolder().toPath().resolve("setup-done.yml");
        this.loader = YamlConfigurationLoader.builder()
                .path(statePath)
                .nodeStyle(NodeStyle.BLOCK)
                .build();
    }

    public boolean completed() {
        try {
            cachedNode = loader.load();
            return cachedNode.node("setup_completed").getBoolean(false);
        } catch (ConfigurateException e) {
            log.warn("<yellow>Could not read setup state, assuming incomplete: " + e.getMessage());
            return false;
        }
    }

    public void markCompleted() {
        try {
            if (cachedNode == null) cachedNode = loader.load();
            cachedNode.node("setup_completed").set(true);
            loader.save(cachedNode);
        } catch (ConfigurateException e) {
            log.error("<red>Failed to persist setup state: " + e.getMessage());
        }
    }
}
