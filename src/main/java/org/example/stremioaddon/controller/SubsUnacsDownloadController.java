package org.example.stremioaddon.controller;

import org.example.stremioaddon.service.SubsUnacsDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subsunacs")
public class SubsUnacsDownloadController {

    private final SubsUnacsDownloadService downloadService;
    private final Logger logger = LoggerFactory.getLogger(SubsUnacsDownloadController.class);

    public SubsUnacsDownloadController(SubsUnacsDownloadService downloadService) {
        this.downloadService = downloadService;
    }

    /**
     * Proxy endpoint for downloading subtitle files from SubsUnacs
     * Returns .srt file directly
     *
     * Format: /subsunacs/download?link={subtitle_url}
     */
    @GetMapping("/download")
    @CrossOrigin
    public ResponseEntity<byte[]> downloadSubtitle(@RequestParam String link) {
        try {
            logger.debug("Received download request for SubsUnacs: {}", link);

            byte[] content = downloadService.downloadSubtitleFile(link);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subtitle.srt\"")
                .body(content);

        } catch (Exception e) {
            logger.error("Failed to download subtitle from SubsUnacs {}: {}", link, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
