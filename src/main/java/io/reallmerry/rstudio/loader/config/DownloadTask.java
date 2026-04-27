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

    public String fileName() {
        return Paths.get(URI.create(url).getPath()).getFileName().toString();
    }
}
