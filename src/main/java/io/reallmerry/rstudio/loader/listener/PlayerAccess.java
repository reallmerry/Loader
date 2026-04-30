package io.reallmerry.rstudio.loader.listener;

import io.reallmerry.rstudio.loader.config.PluginConfig;
import io.reallmerry.rstudio.loader.core.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class PlayerAccess implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final PluginConfig config;
    private final PluginLogger log;

    public PlayerAccess(PluginConfig config, PluginLogger log) {
        this.config = config;
        this.log = log;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!config.whitelist().enabled()) return;
        if (Bukkit.getOfflinePlayer(event.getUniqueId()).isWhitelisted()) return;

        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                buildKickMessage(config.whitelist().rejectionMessages())
        );
        log.info("<gray>Blocked non-whitelisted join: <white>" + event.getName());
    }

    private Component buildKickMessage(List<String> lines) {
        if (lines.isEmpty()) return Component.empty();
        var builder = Component.text();
        for (int i = 0; i < lines.size(); i++) {
            builder.append(MM.deserialize(lines.get(i)));
            if (i < lines.size() - 1) builder.append(Component.newline());
        }
        return builder.build();
    }
}