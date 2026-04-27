package io.reallmerry.rstudio.loader.notification;

import io.reallmerry.rstudio.loader.config.DownloadTask;
import io.reallmerry.rstudio.loader.config.PluginConfig;
import io.reallmerry.rstudio.loader.core.PluginLogger;
import io.reallmerry.rstudio.loader.download.Http;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public final class NotificationDispatcher {

    private final PluginLogger log;
    private final Http http;
    private final PluginConfig.NotificationConfig config;
    private final String serverName;

    public NotificationDispatcher(PluginLogger log, Http http, PluginConfig.NotificationConfig config, String serverName) {
        this.log = log;
        this.http = http;
        this.config = config;
        this.serverName = serverName;
    }

    public void dispatch(List<DownloadTask> tasks, long elapsedMs) {
        if (tasks.isEmpty()) return;

        String pluginNames = tasks.stream()
                .map(DownloadTask::fileName)
                .collect(Collectors.joining(", "));

        String rendered = buildMessage(config.discord().message(), tasks.size(), pluginNames, elapsedMs);

        if (config.discord().enabled()) {
            sendDiscord(rendered);
        }
        if (config.telegram().enabled()) {
            sendTelegram(buildMessage(config.telegram().message(), tasks.size(), pluginNames, elapsedMs));
        }
    }

    private String buildMessage(String template, int count, String plugins, long ms) {
        return template
                .replace("{server_name}", serverName)
                .replace("{count}", String.valueOf(count))
                .replace("{plugins}", plugins)
                .replace("{ms}", String.valueOf(ms));
    }

    private void sendDiscord(String message) {
        String payload = "{\"content\":\"" + escapeJson(message) + "\"}";
        try {
            http.post(config.discord().webhookUrl(), payload, "application/json", Duration.ofSeconds(10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.warn("<yellow>Discord notification failed: " + e.getMessage());
        }
    }

    private void sendTelegram(String message) {
        String url = "https://api.telegram.org/bot" + config.telegram().botToken() + "/sendMessage";
        String payload = "{\"chat_id\":\"" + config.telegram().chatId()
                + "\",\"text\":\"" + escapeJson(message) + "\"}";
        try {
            http.post(url, payload, "application/json", Duration.ofSeconds(10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.warn("<yellow>Telegram notification failed: " + e.getMessage());
        }
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
