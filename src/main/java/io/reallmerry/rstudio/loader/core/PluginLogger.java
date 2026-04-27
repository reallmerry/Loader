package io.reallmerry.rstudio.loader.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class PluginLogger {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String PREFIX = "<dark_red>rStudio <dark_gray>| <reset>";

    private final ComponentLogger logger;

    public PluginLogger(ComponentLogger logger) {
        this.logger = logger;
    }

    public void info(String miniMessage) {
        logger.info(parse(PREFIX + miniMessage));
    }

    public void warn(String miniMessage) {
        logger.warn(parse(PREFIX + miniMessage));
    }

    public void error(String miniMessage) {
        logger.error(parse(PREFIX + miniMessage));
    }

    private static Component parse(String miniMessage) {
        return MM.deserialize(miniMessage);
    }
}
