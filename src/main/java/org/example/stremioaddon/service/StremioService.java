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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Map;

@Service
public class StremioService {
    private final Logger logger = LoggerFactory.getLogger(StremioService.class);


    public SubtitleWrapper mapSubsToStremioStandard(Map<String, SubsUnacsSubtitle> scrappedSubs) {
        SubtitleWrapper subtitleWrapper = new SubtitleWrapper();

        for (SubsUnacsSubtitle sub : scrappedSubs.values()) {
            SubtitleResponse stremioSubs = new SubtitleResponse();

            stremioSubs
                    .setId(sub.getId())
                    .setUrl(sub.getLink())
                    .setLang("Bulgarian");

            subtitleWrapper.getSubtitles()
                    .add(stremioSubs);
        }

        return subtitleWrapper;
    }

    public StreamWrapper mapSubsToStremioStandard(JackResponseWrapper jackResponseWrapper) {
        StreamWrapper streamWrapper = new StreamWrapper();

        for (JackettResponse jackettResponse : jackResponseWrapper.getResult()) {
            try {
                String titleToParse = jackettResponse.getTitle() + ".mkv";
                ParsedFilename parsedFilename = FilenameParser.parseFilename(titleToParse);
                String name = "Angelio" + " " + parsedFilename.resolution;
                String size = String.format("%.2f GB", jackettResponse.getSize() / 1_073_741_824.0);
                String description = String.format("👤 %s | 💾 %s | 🎬 %s",
                        jackettResponse.getSeeders(), size, parsedFilename.resolution);

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

//    public static List<String> getTrackers(String url, String infoHash) {
//        List<String> trackers = new ArrayList<>();
//        trackers.add("dht:" + infoHash.toLowerCase());
//
//
//        String[] magnet = url.split("&tr=");
//        for (int i = 1; i < magnet.length; i++) {
//            String tracker = URLDecoder.decode(magnet[i], StandardCharsets.UTF_8);
//            trackers.add("tracker:" + tracker);
//        }
//
//        return trackers;
//    }
}
