package org.example.stremioaddon.controller;

import org.example.stremioaddon.model.ombd.OmdbResponse;
import org.example.stremioaddon.model.stremio.SubtitleWrapper;
import org.example.stremioaddon.model.subunac.SubsUnacsSubtitle;
import org.example.stremioaddon.service.OmdbService;
import org.example.stremioaddon.service.StremioService;
import org.example.stremioaddon.service.SubsUnacsProviderService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

@RestController
public class SubtitleController {
    private final SubsUnacsProviderService subsUnacsProviderService;
    private final OmdbService omdbService;
    private final StremioService stremioService;
    private final Logger logger = LoggerFactory.getLogger(SubtitleController.class);

    public SubtitleController(SubsUnacsProviderService subsUnacsProviderService,
                              OmdbService omdbService,
                              StremioService stremioService) {
        this.subsUnacsProviderService = subsUnacsProviderService;
        this.omdbService = omdbService;
        this.stremioService = stremioService;
    }


    @GetMapping("/subtitles/{type}/{id}/{extra}.json")
    @CrossOrigin
    public ResponseEntity<SubtitleWrapper> getSubtitles(
            @PathVariable String type,
            @PathVariable String id,
            @PathVariable(required = false) String extra) {

        try {
            String[] stremioId = id.split(":");

            OmdbResponse videoMeta = omdbService.getByImdbId(stremioId[0]);
            logger.debug("Fetched OMDB metadata for id {}: {} ({})",
                    stremioId[0], videoMeta.getTitle(), videoMeta.getYear());

            // Fetch from SubsUnacs
            Map<String, SubsUnacsSubtitle> subsFromSubUnac =
                    subsUnacsProviderService.searchSubtitles(videoMeta, stremioId);
            logger.debug("Found {} subtitles from SubsUnacs", subsFromSubUnac.size());


            // Merge results
            SubtitleWrapper subtitleWrapper = stremioService.mergeSubtitles(
                    subsFromSubUnac
            );

            logger.info("Returning {} total subtitles for {} ({} from SubsUnacs",
                    subtitleWrapper.getSubtitles().size(), videoMeta.getTitle(),
                    subsFromSubUnac.size());

            return ResponseEntity.ok(subtitleWrapper);

        } catch (Exception e) {
            logger.error("Error while fetching subtitles for id {} and type {}: {}",
                    id, type, e.getMessage(), e);
            SubtitleWrapper empty = new SubtitleWrapper();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(empty);
        }
    }
}
