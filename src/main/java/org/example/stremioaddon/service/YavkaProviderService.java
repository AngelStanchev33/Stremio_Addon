package org.example.stremioaddon.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.example.stremioaddon.model.ombd.OmdbResponse;
import org.example.stremioaddon.model.yavka.YavkaSubtitle;
import org.example.stremioaddon.utility.HttpConstants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class YavkaProviderService {

    private static final Logger logger = LoggerFactory.getLogger(YavkaProviderService.class);
    private static final String BASE_URL = "https://yavka.net";
    private static final String IMDB_LOOKUP_URL = "/imdb/";

    private final CacheManager cacheManager;

    public YavkaProviderService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Generate cache key from video metadata (same as SubsUnacs)
     * Level 1 cache key
     */
    private String generateCacheKey(OmdbResponse videoMeta, String[] stremioId) {
        String type = videoMeta.getType().toLowerCase();
        String title = videoMeta.getTitle().toLowerCase();
        String year = videoMeta.getYear();

        String cacheString = type + "-" + title + "-" + year;

        // For series, add season:episode
        if (stremioId.length > 1) {
            cacheString += "-" + stremioId[1] + "-" + stremioId[2];
        }

        return DigestUtils.sha1Hex(cacheString);
    }

    /**
     * Main entry point - searches for subtitles by IMDB ID
     * Level 1 cache - caches entire search result
     */
    public Map<String, YavkaSubtitle> searchSubtitles(
            OmdbResponse videoMeta,
            String[] stremioId) {

        String cacheKey = generateCacheKey(videoMeta, stremioId);
        logger.info("Cache key for '{}' on Yavka: {}", videoMeta.getTitle(), cacheKey);

        var cache = cacheManager.getCache("yavkaSearch");
        @SuppressWarnings("unchecked")
        Map<String, YavkaSubtitle> cached = (Map<String, YavkaSubtitle>) cache.get(cacheKey, Map.class);
        if (cached != null) {
            logger.info("Cache HIT for yavka key: {} ({} entries)", cacheKey, cached.size());
            return cached;
        }
        logger.info("Cache MISS for yavka key: {}", cacheKey);

        Map<String, YavkaSubtitle> subtitles = new HashMap<>();

        try {
            String imdbId = stremioId[0];  // e.g., "tt1234567"
            logger.debug("Searching yavka.net for IMDB ID: {} (cache key: {})", imdbId, cacheKey);

            // Direct lookup: GET https://yavka.net/imdb/tt1234567
            Document doc = fetchImdbPage(imdbId);

            // Parse subtitle rows and create YavkaSubtitle objects directly
            subtitles = parseSubtitles(doc, videoMeta);

            logger.debug("Found {} subtitles on yavka.net for {}", subtitles.size(), videoMeta.getTitle());

        } catch (Exception e) {
            logger.error("Error searching yavka.net: {}", e.getMessage(), e);
        }

        // Cache the results
        if (!subtitles.isEmpty()) {
            cache.put(cacheKey, subtitles);
            logger.info("Stored {} subtitles in yavka cache with key: {}", subtitles.size(), cacheKey);
        } else {
            logger.warn("No subtitles found on yavka.net — NOT caching empty result for key: {}", cacheKey);
        }

        return subtitles;
    }

    /**
     * Fetch IMDB page with subtitle listings
     */
    private Document fetchImdbPage(String imdbId) throws IOException {
        String url = BASE_URL + IMDB_LOOKUP_URL + imdbId;

        Connection.Response response = Jsoup.connect(url)
            .userAgent(getRandomUserAgent())
            .header("Accept", HttpConstants.HEADER_ACCEPT)
            .header("Accept-Language", HttpConstants.HEADER_ACCEPT_LANGUAGE)
            .header("Accept-Encoding", HttpConstants.HEADER_ACCEPT_ENCODING)
            .header("DNT", HttpConstants.HEADER_DNT)
            .header("Upgrade-Insecure-Requests", HttpConstants.HEADER_UPGRADE_INSECURE)
            .header("Cache-Control", HttpConstants.HEADER_CACHE_CONTROL)
            .referrer(BASE_URL + "/")
            .timeout(HttpConstants.TIMEOUT_STANDARD)
            .followRedirects(true)
            .ignoreHttpErrors(true)
            .execute();

        // Check if blocked (403/429/etc)
        if (response.statusCode() != 200) {
            logger.warn("Yavka.net returned status {}: {}", response.statusCode(), url);
            // Return empty document instead of throwing exception
            return Jsoup.parse("<html></html>");
        }

        return response.parse();
    }

    /**
     * Parse HTML table rows and create YavkaSubtitle objects directly
     * No intermediate SubtitleRow class - follows SubsUnacs pattern
     */
    private Map<String, YavkaSubtitle> parseSubtitles(Document doc, OmdbResponse videoMeta) {
        Map<String, YavkaSubtitle> subtitles = new HashMap<>();
        Elements tableRows = doc.select("tr");

        // Process last N rows (as per Python implementation)
        int startIndex = Math.max(0, tableRows.size() - HttpConstants.YAVKA_MAX_ROWS_TO_PROCESS);

        for (int i = startIndex; i < tableRows.size(); i++) {
            Element row = tableRows.get(i);

            // Look for a.balon or a.selector
            Element linkElement = row.selectFirst("a.balon, a.selector");
            if (linkElement == null) continue;

            // Extract all subtitle metadata
            String link = linkElement.attr("href");
            String title = linkElement.text();

            // Extract notes from content attribute
            String content = linkElement.attr("content");
            String notes = extractNotesFromContent(content);

            // Extract year from next sibling span
            Integer year = null;
            Element yearSpan = linkElement.nextElementSibling();
            if (yearSpan != null && "span".equals(yearSpan.tagName())) {
                String yearText = yearSpan.text().replaceAll("[()]", "").trim();
                try {
                    year = Integer.parseInt(yearText);
                } catch (NumberFormatException e) {
                    // Year is optional
                }
            }

            // Extract FPS from span with title="Кадри в секунда"
            Float fps = null;
            Element fpsElement = row.selectFirst("span[title=Кадри в секунда]");
            if (fpsElement != null) {
                try {
                    fps = Float.parseFloat(fpsElement.text().trim());
                } catch (NumberFormatException e) {
                    // FPS is optional
                }
            }

            // Extract uploader from a.click
            String uploader = null;
            Element uploaderElement = row.selectFirst("a.click");
            if (uploaderElement != null) {
                uploader = uploaderElement.text();
            }

            // Create YavkaSubtitle object directly
            YavkaSubtitle subtitle = new YavkaSubtitle()
                .setId(link)  // Use link as unique ID
                .setFilename(title)
                .setLink(link)
                .setType(videoMeta.getType())
                .setTitle(title)
                .setNotes(notes)
                .setYear(year)
                .setFps(fps)
                .setUploader(uploader);

            subtitles.put(subtitle.getId(), subtitle);
        }

        return subtitles;
    }

    /**
     * Visit subtitle page and extract POST form data
     * PUBLIC - called by YavkaDownloadService
     * Caches form data separately from search results
     */
    public Map<String, String> getFormDataForLink(String link) throws IOException, InterruptedException {
        String cacheKey = DigestUtils.sha1Hex(link);

        var cache = cacheManager.getCache("yavkaFormData");
        @SuppressWarnings("unchecked")
        Map<String, String> cached = (Map<String, String>) cache.get(cacheKey, Map.class);
        if (cached != null) {
            logger.debug("Cache HIT for yavka form data: {}", link);
            return cached;
        }

        logger.debug("Cache MISS for yavka form data: {}", link);

        // Slow down to prevent throttling
        Thread.sleep(randomDelay());

        String fullUrl = BASE_URL + link;
        Document doc = Jsoup.connect(fullUrl)
            .userAgent(getRandomUserAgent())
            .header("Accept", HttpConstants.HEADER_ACCEPT)
            .header("Accept-Language", HttpConstants.HEADER_ACCEPT_LANGUAGE)
            .header("Accept-Encoding", HttpConstants.HEADER_ACCEPT_ENCODING)
            .header("DNT", HttpConstants.HEADER_DNT)
            .header("Upgrade-Insecure-Requests", HttpConstants.HEADER_UPGRADE_INSECURE)
            .header("Cache-Control", HttpConstants.HEADER_CACHE_CONTROL)
            .referrer(BASE_URL + "/")
            .timeout(HttpConstants.TIMEOUT_STANDARD)
            .followRedirects(true)
            .get();

        // Find POST form
        Element form = doc.selectFirst("form[method=POST]");
        if (form == null) {
            logger.warn("No POST form found on subtitle page: {}", link);
            return Collections.emptyMap();
        }

        // Extract all input fields
        Map<String, String> formData = new HashMap<>();
        Elements inputs = form.select("input");
        for (Element input : inputs) {
            String name = input.attr("name");
            if (name != null && !name.isEmpty()) {
                String value = input.attr("value");
                formData.put(name, value != null ? value : "");
            }
        }

        logger.debug("Extracted form data for {}: {}", link, formData);

        // Cache the result
        cache.put(cacheKey, formData);

        return formData;
    }

    private static final Random RANDOM = new Random();

    private String getRandomUserAgent() {
        return HttpConstants.USER_AGENTS.get(RANDOM.nextInt(HttpConstants.USER_AGENTS.size()));
    }

    private int randomDelay() {
        return RANDOM.nextInt(HttpConstants.MAX_DELAY);
    }

    private String extractNotesFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // Extract text from HTML content attribute
        return Jsoup.parse(content).text();
    }
}
