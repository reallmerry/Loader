package io.reallmerry.rstudio.loader.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class DownloadTaskSerializer implements TypeSerializer<DownloadTask> {

    public static final DownloadTaskSerializer INSTANCE = new DownloadTaskSerializer();

    private DownloadTaskSerializer() {}

    @Override
    public DownloadTask deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (!node.isMap()) {
            String url = node.getString();
            if (url == null || url.isBlank()) {
                throw new SerializationException(node, type, "Empty URL in plugin list");
            }
            return DownloadTask.of(url);
        }

        String url = node.node("url").getString();
        if (url == null || url.isBlank()) {
            throw new SerializationException(node, type, "Plugin entry is missing 'url'");
        }

        String sha256 = node.node("sha256").getString();

        ConfigurationNode headersNode = node.node("headers");
        Map<String, String> headers = new HashMap<>();
        if (!headersNode.empty() && headersNode.isMap()) {
            for (var entry : headersNode.childrenMap().entrySet()) {
                String value = entry.getValue().getString();
                if (value != null) {
                    headers.put(entry.getKey().toString(), value);
                }
            }
        }

        return new DownloadTask(url, Map.copyOf(headers), sha256);
    }

    @Override
    public void serialize(Type type, DownloadTask task, ConfigurationNode node) throws SerializationException {
        if (task.headers().isEmpty() && task.sha256() == null) {
            node.set(task.url());
            return;
        }

        node.node("url").set(task.url());
        if (task.sha256() != null) {
            node.node("sha256").set(task.sha256());
        }
        if (!task.headers().isEmpty()) {
            ConfigurationNode headersNode = node.node("headers");
            for (var entry : task.headers().entrySet()) {
                headersNode.node(entry.getKey()).set(entry.getValue());
            }
        }
    }
}
