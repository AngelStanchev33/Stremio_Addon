package org.example.stremioaddon.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.example.stremioaddon.model.reponse.OmdbResponse;
import org.example.stremioaddon.model.subunac.Episode;
import org.example.stremioaddon.model.subunac.SubsUnacsSubtitle;
import org.example.stremioaddon.model.subunac.Video;
import org.example.stremioaddon.utility.SanitizeText;
import org.example.stremioaddon.utility.TitleNormalization;
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
import java.util.*;

@Service
public class SubsUnacsProviderService {
    private static final String BASE_URL = "https://subsunacs.net";
    private static final String SEARCH_URL = "/search.php";
    private final Logger logger = LoggerFactory.getLogger(SubsUnacsProviderService.class);
    private final CacheManager cacheManager;
    private final SanitizeText sanitizeText;
    private final TitleNormalization titleNormalization;
    private final Map<String, String> cookies = new HashMap<>();

    private static final List<String> USER_AGENTS = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");

    public SubsUnacsProviderService(CacheManager cacheManager, SanitizeText sanitizeText,
                                    TitleNormalization titleNormalization) {
        this.cacheManager = cacheManager;
        this.sanitizeText = sanitizeText;
        this.titleNormalization = titleNormalization;
    }

    private String getRandomUserAgent() {
        Random random = new Random();
        return USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
    }

    public Connection createConnection(String url) {
        String userAgent = getRandomUserAgent();

        logger.debug("Creating connection to: {}", url);

        return Jsoup.connect(url)
                .userAgent(userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("DNT", "1")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Cache-Control", "max-age=0")
                .cookies(cookies)
                .timeout(10000)
                .ignoreHttpErrors(true);
    }

    private String buildCacheKey(Video video) {
        StringBuilder sb = new StringBuilder();
        sb.append(video.getType()).append('|');
        sb.append(video.getTitle() != null ? video.getTitle().toLowerCase() : "").append('|');

        if ("episode".equals(video.getType()) && video instanceof Episode ep) {
            sb.append(ep.getSeason()).append('|')
                    .append(ep.getEpisode()).append('|')
                    .append(ep.getYear() != null ? ep.getYear() : "");
        } else {
            sb.append(video.getYear() != null ? video.getYear() : "");
        }

        String cacheInput = sb.toString();
        return DigestUtils.sha1Hex(cacheInput);
    }

    public Map<String, SubsUnacsSubtitle> searchSubtitles(OmdbResponse omdbResponse, String[] stremioId) {
        Video video = new Video(omdbResponse.getTitle(), omdbResponse.getType(), omdbResponse.getYear());

        String cacheKey = buildCacheKey(video);
        logger.info("Cache key for '{}' (type={}, year={}): {}", video.getTitle(), video.getType(), video.getYear(),
                cacheKey);

        var cache = cacheManager.getCache("subsUnacsSearch");
        if (cache != null) {
            Map<String, SubsUnacsSubtitle> cached = cache.get(cacheKey, Map.class);
            if (cached != null) {
                logger.info("Cache HIT for key: {} ({} entries)", cacheKey, cached.size());
                return cached;
            }
            logger.info("Cache MISS for key: {}", cacheKey);
        } else {
            logger.warn("Cache 'subsUnacsSearch' is NULL — caching is not working!");
        }

        // --- Тук остава твоята текуща логика ---
        Map<String, SubsUnacsSubtitle> subtitles = new HashMap<>();

        Map<String, String> params = new HashMap<>();
        params.put("l", "0");
        params.put("c", "");
        params.put("action", " Търси ");
        params.put("a", "");
        params.put("d", "");
        params.put("u", "");
        params.put("g", "");
        params.put("t", "");
        params.put("imdbcheck", "1");

        if ("series".equals(video.getType())) {
            Episode ep = new Episode(video.getTitle(), video.getType(), video.getYear(),
                    Integer.parseInt(stremioId[1]), Integer.parseInt(stremioId[2]));
            String seriesName = sanitizeText.sanitize(titleNormalization.fixTvNaming(ep.getTitle()));
            params.put("m", String.format("%s %02d %02d",
                    seriesName, Integer.parseInt(stremioId[1]), Integer.parseInt(stremioId[2])));
            params.put("y", ep.getYear());
            logger.info("Searching subtitles for: {} S{}E{}", ep.getTitle(), ep.getSeason(), ep.getEpisode());
        } else {
            String movieTitle = sanitizeText.sanitize(titleNormalization.fixMovieNaming(video.getTitle()));
            params.put("m", movieTitle);
            params.put("y", video.getYear() != null ? video.getYear() : "");
            logger.info("Searching subtitles for: {} ({})", video.getTitle(), video.getYear());
        }

        try {
            Connection.Response response = createConnection(BASE_URL + SEARCH_URL)
                    .data(params)
                    .method(Connection.Method.POST)
                    .referrer("https://subsunacs.net/index.php")
                    .timeout(10000)
                    .followRedirects(false)
                    .execute();

            if (response.statusCode() != 200) {
                logger.debug("No subtitles found, status: {}", response.statusCode());
                return subtitles;
            }

            Document doc = response.parse();
            Elements rows = doc.select("tr[onmouseover]");
            logger.info("Found {} subtitle rows", rows.size());
            int limit = Math.min(rows.size(), 20);

            for (int i = 0; i < limit; i++) {
                Element row = rows.get(i);
                Element tdMovie = row.selectFirst("td.tdMovie");
                if (tdMovie == null)
                    continue;

                Element linkElement = tdMovie.selectFirst("a.tooltip");
                if (linkElement == null)
                    continue;

                String pageLink = linkElement.attr("href");

                try {
                    Document subtitlePage = Jsoup.connect(BASE_URL + pageLink)
                            .timeout(5000)
                            .get();

                    Element rarview = subtitlePage.selectFirst("div.rarview");
                    if (rarview == null) {
                        logger.warn("rarview not found for {}", pageLink);
                        continue;
                    }

                    Elements labels = rarview.select("label a");
                    String srtFileName = null;
                    String downloadUrl = null;

                    for (Element a : labels) {
                        String fileName = a.text();
                        String href = a.attr("href");
                        logger.debug("Archive entry: {} -> {}", fileName, href);

                        if (fileName.endsWith(".srt")) {
                            srtFileName = fileName;
                            downloadUrl = href.startsWith("http") ? href : BASE_URL + href;
                            break;
                        }
                    }

                    if (srtFileName == null) {
                        logger.warn("No .srt found in archive for {}", pageLink);
                        continue;
                    }

                    SubsUnacsSubtitle subtitle = new SubsUnacsSubtitle()
                            .setFilename(srtFileName)
                            .setLink(downloadUrl)
                            .setType(video.getType());

                    subtitles.putIfAbsent(subtitle.getFilename(), subtitle);

                } catch (Exception e) {
                    logger.error("Error processing subtitle page {}: {}", pageLink, e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            logger.error("Error searching subtitles on SubsUnacs: {}", e.getMessage(), e);
        }

        // --- ТУК е cache.put(...) ---
        if (cache != null && !subtitles.isEmpty()) {
            cache.put(cacheKey, subtitles);
            logger.info("Stored {} subtitles in cache with key: {}", subtitles.size(), cacheKey);
        } else if (subtitles.isEmpty()) {
            logger.warn("No subtitles found — NOT caching empty result for key: {}", cacheKey);
        }

        return subtitles;
    }

}
