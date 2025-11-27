package io.reallmerry.rstudio.loader.managers;

import io.reallmerry.rstudio.loader.core.LoggerManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Downloader {

    private final LoggerManager log;

    public Downloader(LoggerManager log) {
        this.log = log;
    }

    public void downloadPlugins(List<String> urls) {
        for (String urlString : urls) {
            try {
                URL url = new URL(urlString);
                String fileName = Paths.get(url.getPath()).getFileName().toString();
                log.msg("<white>Downloading &7" + fileName + " &f...");
                Path destination = Paths.get("plugins", fileName);

                if (!Files.exists(destination.getParent())) {
                    Files.createDirectories(destination.getParent());
                }

                try (InputStream in = url.openStream()) {
                    Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
                }

                //log.msg("<white>File " + fileName + " successfully loaded.");
            } catch (IOException e) {
                log.error("<red>Error during installation " + urlString + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}