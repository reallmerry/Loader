package io.reallmerry.rstudio.loader.config;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

public record DownloadTask(
        String url,
        Map<String, String> headers,
        @Nullable String sha256
) {
    public static DownloadTask of(String url) {
        return new DownloadTask(url, Map.of(), null);
    }

    public boolean hasValidHash() { return sha256 != null && !sha256.isBlank() && !sha256.equalsIgnoreCase("null"); }

    public String fileName() {
        var path = Paths.get(URI.create(url).getPath());
        var name = path.getFileName();
        if (name == null) throw new IllegalArgumentException("Cannot resolve filename from URL: " + url);
        return name.toString();
    }
}
