package io.reallmerry.rstudio.loader.utils;

import org.bukkit.ChatColor;

public class ColorUtil {

    public static String parseColor(String msg) {
        msg = msg.replace("<yellow>", "§e")
                .replace("<gold>", "§6")
                .replace("<green>", "§a")
                .replace("<red>", "§c")
                .replace("<gray>", "§7")
                .replace("<dark_gray>", "§8")
                .replace("<white>", "§f")
                .replace("<blue>", "§9")
                .replace("<dark_blue>", "§1")
                .replace("<aqua>", "§b")
                .replace("<dark_aqua>", "§3")
                .replace("<light_purple>", "§d")
                .replace("<dark_purple>", "§5")
                .replace("<dark_red>", "§4")
                .replace("<dark_green>", "§2")
                .replace("<bold>", "§b");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}