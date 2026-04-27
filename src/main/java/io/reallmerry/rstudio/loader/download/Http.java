package io.reallmerry.rstudio.loader.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class Http {

    private final HttpClient client;

    public Http(Duration connectTimeout) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HttpResponse<String> get(String url, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {

        HttpRequest request = buildRequest(url, headers, timeout).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<InputStream> download(String url, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {

        HttpRequest request = buildRequest(url, headers, timeout).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    public HttpResponse<String> post(String url, String body, String contentType, Duration timeout)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder buildRequest(String url, Map<String, String> headers, Duration timeout) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout);
        headers.forEach(builder::header);
        return builder;
    }
}
