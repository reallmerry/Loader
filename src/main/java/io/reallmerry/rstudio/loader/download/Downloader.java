package io.reallmerry.rstudio.loader.download;

import io.reallmerry.rstudio.loader.config.DownloadTask;

import java.util.List;

public interface Downloader {

    long downloadAll(List<DownloadTask> tasks);
}
