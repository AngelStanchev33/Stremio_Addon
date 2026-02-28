package org.example.stremioaddon.service;

import org.example.stremioaddon.utility.HttpConstants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Service
public class YavkaDownloadService {

    private final YavkaProviderService yavkaProviderService;
    private static final Logger logger = LoggerFactory.getLogger(YavkaDownloadService.class);

    public YavkaDownloadService(YavkaProviderService yavkaProviderService) {
        this.yavkaProviderService = yavkaProviderService;
    }

    /**
     * Downloads archive from yavka.net using POST with form data
     * Cached for 24 hours - Level 2 cache
     */
    @Cacheable(value = "yavkaArchives", key = "#link")
    public byte[] downloadArchive(String link) throws IOException, InterruptedException {
        // Get form data from provider service (cached)
        Map<String, String> formData = yavkaProviderService.getFormDataForLink(link);

        if (formData.isEmpty()) {
            throw new IOException("No form data found for link: " + link);
        }

        logger.debug("Downloading archive with form data: {}", formData.keySet());

        // Add random delay to prevent throttling
        Thread.sleep(new Random().nextInt(HttpConstants.MAX_DELAY));

        // POST request to download archive
        String fullUrl = "https://yavka.net" + link;
        Connection.Response response = Jsoup.connect(fullUrl)
            .data(formData)
            .method(Connection.Method.POST)
            .referrer(fullUrl)
            .userAgent(getRandomUserAgent())
            .header("Accept", HttpConstants.HEADER_ACCEPT)
            .header("Accept-Language", HttpConstants.HEADER_ACCEPT_LANGUAGE)
            .header("Accept-Encoding", HttpConstants.HEADER_ACCEPT_ENCODING)
            .header("DNT", HttpConstants.HEADER_DNT)
            .header("Origin", "https://yavka.net")
            .ignoreContentType(true)
            .maxBodySize(HttpConstants.MAX_ARCHIVE_SIZE_BYTES)
            .timeout(HttpConstants.TIMEOUT_DOWNLOAD)
            .followRedirects(true)
            .execute();

        byte[] archiveBytes = response.bodyAsBytes();
        logger.debug("Downloaded archive: {} bytes", archiveBytes.length);

        return archiveBytes;
    }

    private String getRandomUserAgent() {
        return HttpConstants.USER_AGENTS.get(new Random().nextInt(HttpConstants.USER_AGENTS.size()));
    }
}
