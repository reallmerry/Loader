package io.reallmerry.rstudio.loader.managers;

import io.reallmerry.rstudio.loader.Loader;
import io.reallmerry.rstudio.loader.core.ConfigManager;
import io.reallmerry.rstudio.loader.core.LoggerManager;
import io.reallmerry.rstudio.loader.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Arrays;
import java.util.List;

public class WhitelistManager implements Listener {
    private final LoggerManager log;
    private final List<String> rejectionMessages;

    public WhitelistManager(Loader plugin, LoggerManager log, ConfigManager config) {
        this.log = log;
        String[] configMessages = config.getRejectionMessages();
        this.rejectionMessages = Arrays.asList(configMessages);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (Bukkit.hasWhitelist() && !Bukkit.getOfflinePlayer(event.getUniqueId()).isWhitelisted()) {
            log.msg("<gray>Connection attempt to the closed server with nickname <red>" + event.getName() + " <gray>(UUID: <red>" + event.getUniqueId() + "<gray>). Connection <red>rejected<gray>.");
            String kickMessage = getRandomRejectionMessage();
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage);
        }
    }

    private String getRandomRejectionMessage() {
        if (rejectionMessages.isEmpty()) {
            return "Access denied. Reason unknown.";
        }
        int randomIndex = (int) (Math.random() * rejectionMessages.size());
        return ColorUtil.parseColor(rejectionMessages.get(randomIndex));
    }
}