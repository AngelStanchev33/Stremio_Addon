package org.example.stremioaddon.controller;

import org.example.stremioaddon.model.jackett.JackResponseWrapper;
import org.example.stremioaddon.model.ombd.OmdbResponse;
import org.example.stremioaddon.model.stremio.StreamWrapper;
import org.example.stremioaddon.service.JackettService;
import org.example.stremioaddon.service.OmdbService;
import org.example.stremioaddon.service.StremioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class StreamController {
    private final Logger logger = LoggerFactory.getLogger(StreamController.class);

    private final JackettService jackettService;
    private final OmdbService omdbService;
    private final StremioService stremioService;

    public StreamController(JackettService jackettService, OmdbService omdbService, StremioService stremioService) {
        this.jackettService = jackettService;
        this.omdbService = omdbService;
        this.stremioService = stremioService;
    }

    @GetMapping("/stream/{type}/{id}.json")
    @CrossOrigin
    public ResponseEntity<StreamWrapper> defineStreamHandler(
            @PathVariable String type,
            @PathVariable String id
    ) {
        logger.debug("Hit stream endpoint");

        try {
            OmdbResponse videoMeta = omdbService.getByImdbId(id);
            logger.debug("Fetched OMDB metadata for id {}, name {}, year {}, and type {}",
                    videoMeta.getImdbID(),
                    videoMeta.getTitle(),
                    videoMeta.getYear(),
                    videoMeta.getType());
            String typeOfMedia = omdbService.getType(videoMeta.getType());

            JackResponseWrapper jackettResponse =
                    jackettService.findSteams(videoMeta.getTitle(), videoMeta.getYear(), typeOfMedia);
            logger.debug("Found streams for {} from Jackett", videoMeta.getTitle());

            StreamWrapper response = stremioService.mapSubsToStremioStandard(jackettResponse);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS)
                            .staleWhileRevalidate(86400, TimeUnit.SECONDS)
                    )
                    .body(response);

        } catch (Exception e) {
            logger.error("Error while fetching streams for id {} and type {}: {}",
                    id, type, e.getMessage());
            StreamWrapper empty = new StreamWrapper();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(empty);
        }
    }
}
