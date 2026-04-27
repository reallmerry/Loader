package io.reallmerry.rstudio.loader.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public final class PluginConfig {

    private AdminConfig admin = new AdminConfig();
    private WorldConfig world = new WorldConfig();
    private WhitelistConfig whitelist = new WhitelistConfig();
    private PluginsConfig plugins = new PluginsConfig();
    private NotificationConfig notifications = new NotificationConfig();

    public AdminConfig admin() { return admin; }
    public WorldConfig world() { return world; }
    public WhitelistConfig whitelist() { return whitelist; }
    public PluginsConfig plugins() { return plugins; }
    public NotificationConfig notifications() { return notifications; }

    @ConfigSerializable
    public record AdminConfig(
            @Setting("op-players") List<String> opPlayers,
            @Setting("notify-about-updates") boolean notifyAboutUpdates
    ) {
        public AdminConfig() {
            this(List.of("reallmerry"), true);
        }
    }

    @ConfigSerializable
    public record WorldConfig(
            String difficulty,
            int time,
            @Setting("freeze-time") boolean freezeTime,
            @Setting("disable-weather") boolean disableWeather,
            @Setting("disable-thunder") boolean disableThunder
    ) {
        public WorldConfig() {
            this("PEACEFUL", 6000, true, true, true);
        }
    }

    @ConfigSerializable
    public record WhitelistConfig(
            boolean enabled,
            @Setting("rejection-messages") List<String> rejectionMessages
    ) {
        public WhitelistConfig() {
            this(true, List.of(
                    "",
                    "<red><bold>rStudio</bold></red>",
                    "",
                    "<white>Infrastructure Unavailable",
                    "<white>Operator authorization credentials required for network entry.",
                    "",
                    "<gray>Access denied. Contact server administration."
            ));
        }
    }

    @ConfigSerializable
    public static final class PluginsConfig {
        @Setting("auto-download") private boolean autoDownload = true;
        private List<DownloadTask> list = new ArrayList<>();
        @Setting("backup-retain-count") private int backupRetainCount = 3;

        public boolean autoDownload() { return autoDownload; }
        public List<DownloadTask> list() { return list; }
        public int backupRetainCount() { return backupRetainCount; }
    }

    @ConfigSerializable
    public record NotificationConfig(
            WebhookConfig discord,
            TelegramConfig telegram
    ) {
        public NotificationConfig() {
            this(new WebhookConfig(), new TelegramConfig());
        }
    }

    @ConfigSerializable
    public record WebhookConfig(
            boolean enabled,
            @Setting("webhook-url") String webhookUrl,
            String message
    ) {
        public WebhookConfig() {
            this(false, "", "✅ {server_name}: loaded {count} plugins in {ms}ms\n{plugins}");
        }
    }

    @ConfigSerializable
    public record TelegramConfig(
            boolean enabled,
            @Setting("bot-token") String botToken,
            @Setting("chat-id") String chatId,
            String message
    ) {
        public TelegramConfig() {
            this(false, "", "", "✅ {server_name}: loaded {count} plugins in {ms}ms\n{plugins}");
        }
    }
}