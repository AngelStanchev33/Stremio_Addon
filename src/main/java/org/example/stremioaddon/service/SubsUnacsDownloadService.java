package org.example.stremioaddon.service;

import org.example.stremioaddon.utility.HttpConstants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Random;

@Service
public class SubsUnacsDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(SubsUnacsDownloadService.class);

    /**
     * Downloads .srt file from SubsUnacs
     * Level 2 cache - caches actual file content
     */
    @Cacheable(value = "subsUnacsFiles", key = "#link")
    public byte[] downloadSubtitleFile(String link) throws IOException {
        logger.debug("Downloading subtitle file from SubsUnacs: {}", link);

        Connection.Response response = Jsoup.connect(link)
            .userAgent(getRandomUserAgent())
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Referer", "https://subsunacs.net/")
            .ignoreContentType(true)
            .timeout(10000)
            .execute();

        byte[] content = response.bodyAsBytes();
        logger.debug("Downloaded {} bytes from SubsUnacs", content.length);

        return content;
    }

    private String getRandomUserAgent() {
        return HttpConstants.USER_AGENTS.get(new Random().nextInt(HttpConstants.USER_AGENTS.size()));
    }
}
