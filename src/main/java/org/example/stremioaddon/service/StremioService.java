package org.example.stremioaddon.service;

import io.github.mdaubie.torrentnameparser.FilenameParser;
import io.github.mdaubie.torrentnameparser.model.ParsedFilename;
import org.example.stremioaddon.model.jackett.JackResponseWrapper;
import org.example.stremioaddon.model.jackett.JackettResponse;
import org.example.stremioaddon.model.stremio.StreamResponse;
import org.example.stremioaddon.model.stremio.StreamWrapper;
import org.example.stremioaddon.model.stremio.SubtitleResponse;
import org.example.stremioaddon.model.stremio.SubtitleWrapper;
import org.example.stremioaddon.model.subunac.SubsUnacsSubtitle;
import org.example.stremioaddon.model.yavka.YavkaSubtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class StremioService {
    private final Logger logger = LoggerFactory.getLogger(StremioService.class);

    public StreamWrapper mapToStremioStandard(JackResponseWrapper jackResponseWrapper) {
        StreamWrapper streamWrapper = new StreamWrapper();

        for (JackettResponse jackettResponse : jackResponseWrapper.getResult()) {
            try {
                String titleToParse = jackettResponse.getTitle() + ".mkv";
                ParsedFilename parsedFilename = FilenameParser.parseFilename(titleToParse);
                String resolution = parsedFilename.resolution != null
                    ? parsedFilename.resolution
                    : jackettResponse.getCategory() != null && jackettResponse.getCategory().contains("/")
                        ? jackettResponse.getCategory().split("/")[1]
                        : "Unknown";
                String name = "Angelio " + resolution;
                String size = jackettResponse.getSize() != null
                    ? String.format("%.2f GB", jackettResponse.getSize() / 1_073_741_824.0)
                    : "Unknown";
                String description = String.format("👤 %s | 💾 %s | 🎬 %s",
                        jackettResponse.getSeeders(), size, resolution);

                StreamResponse streamResponse = new StreamResponse()
                        .setName(name)
                        .setDescription(description)
                        .setUrl(jackettResponse.getUrl());

                logger.debug("""

                                --- STREMIO STREAM ---
                                📦 Name:        {}
                                📝 Description: {}
                                🔑 url:    {}
                                ----------------------""",
                        streamResponse.getName(),
                        streamResponse.getDescription(),
                        streamResponse.getUrl());

                streamWrapper.getStreams().add(streamResponse);

            } catch (InvalidParameterException e) {
                logger.warn("Could not parse torrent name: {}", jackettResponse.getTitle());
            }
        }

        logger.debug("Returned steams to Stremio are {}", streamWrapper.getStreams().size());

        return streamWrapper;
    }

    /**
     * Merge subtitles from multiple providers (SubsUnacs + Yavka)
     * Converts to Stremio format with proper proxy URLs
     */
    public SubtitleWrapper mergeSubtitles(
            Map<String, SubsUnacsSubtitle> subsUnacsSubtitles,
            Map<String, YavkaSubtitle> yavkaSubtitles) {

        SubtitleWrapper wrapper = new SubtitleWrapper();
        Set<SubtitleResponse> allSubtitles = new HashSet<>();

        // Add SubsUnacs subtitles
        for (SubsUnacsSubtitle sub : subsUnacsSubtitles.values()) {
            // Build download URL using proxy endpoint
            String downloadUrl = String.format(
                "http://localhost:8080/subsunacs/download?link=%s",
                URLEncoder.encode(sub.getLink(), StandardCharsets.UTF_8)
            );

            SubtitleResponse stremioSub = new SubtitleResponse()
                .setId(sub.getId())
                .setUrl(downloadUrl)
                .setLang("Bulgarian");
            allSubtitles.add(stremioSub);
        }

        // Add Yavka subtitles
        for (YavkaSubtitle sub : yavkaSubtitles.values()) {
            // Build download URL using proxy endpoint (returns archive)
            String downloadUrl = String.format(
                "http://localhost:8080/yavka/download?link=%s",
                URLEncoder.encode(sub.getLink(), StandardCharsets.UTF_8)
            );

            SubtitleResponse stremioSub = new SubtitleResponse()
                .setId(sub.getId())
                .setUrl(downloadUrl)  // Points to RAR/ZIP archive
                .setLang("Bulgarian");
            allSubtitles.add(stremioSub);
        }

        wrapper.setSubtitles(allSubtitles);
        logger.debug("Merged {} SubsUnacs + {} Yavka = {} total subtitles",
            subsUnacsSubtitles.size(), yavkaSubtitles.size(), allSubtitles.size());

        return wrapper;
    }

}
