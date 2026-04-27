package io.reallmerry.rstudio.loader.listener;

import io.reallmerry.rstudio.loader.config.PluginConfig;
import io.reallmerry.rstudio.loader.core.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
        PluginConfig.WhitelistConfig wc = config.whitelist();
        if (!wc.enabled()) return;

        if (config.admin().opPlayers().contains(event.getName())) return;

        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                buildKickMessage(wc.rejectionMessages())
        );
        log.info("<gray>Blocked non-whitelisted join: <white>" + event.getName());
    }

    private Component buildKickMessage(List<String> lines) {
        if (lines.isEmpty()) return Component.empty();

        int offset = ThreadLocalRandom.current().nextInt(lines.size());
        var builder = Component.text();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get((i + offset) % lines.size());
            builder.append(MM.deserialize(line));
            if (i < lines.size() - 1) builder.append(Component.newline());
        }
        return builder.build();
    }
}