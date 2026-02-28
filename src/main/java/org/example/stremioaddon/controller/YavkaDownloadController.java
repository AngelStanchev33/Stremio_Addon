package org.example.stremioaddon.controller;

import org.example.stremioaddon.service.YavkaDownloadService;
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
@RequestMapping("/yavka")
public class YavkaDownloadController {

    private final YavkaDownloadService yavkaDownloadService;
    private final Logger logger = LoggerFactory.getLogger(YavkaDownloadController.class);

    public YavkaDownloadController(YavkaDownloadService yavkaDownloadService) {
        this.yavkaDownloadService = yavkaDownloadService;
    }

    /**
     * Proxy endpoint for downloading subtitle archives from yavka.net
     * Returns RAR/ZIP archive directly - Stremio handles extraction
     *
     * Format: /yavka/download?link={subtitle_page_url}
     */
    @GetMapping("/download")
    @CrossOrigin
    public ResponseEntity<byte[]> downloadArchive(@RequestParam String link) {
        try {
            logger.debug("Downloading archive from yavka.net: {}", link);

            byte[] archiveBytes = yavkaDownloadService.downloadArchive(link);

            // Detect content type (RAR or ZIP)
            String contentType = detectArchiveType(archiveBytes);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subtitle.rar\"")
                .body(archiveBytes);

        } catch (Exception e) {
            logger.error("Failed to download archive from {}: {}", link, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String detectArchiveType(byte[] bytes) {
        if (bytes.length >= 7 &&
            bytes[0] == 0x52 && bytes[1] == 0x61 && bytes[2] == 0x72) {
            return "application/x-rar-compressed";
        } else if (bytes.length >= 4 &&
                   bytes[0] == 0x50 && bytes[1] == 0x4B) {
            return "application/zip";
        }
        return "application/octet-stream";
    }
}
