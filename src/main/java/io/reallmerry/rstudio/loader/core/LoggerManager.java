package io.reallmerry.rstudio.loader.core;

import io.reallmerry.rstudio.loader.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class LoggerManager {

    private final Plugin plugin;
    private final String prefix;

    public LoggerManager(Plugin plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = ColorUtil.parseColor(prefix);
    }

    public void msg(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ColorUtil.parseColor("<white>" + message));
    }

    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ColorUtil.parseColor("<yellow>" + message));
    }

    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage(prefix + ColorUtil.parseColor("<red>" + message));
    }

    public void log(String message) {
        msg(message);
    }
}