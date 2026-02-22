package org.example.stremioaddon.controller;

import org.example.stremioaddon.config.Manifest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManifestController {
    private final Manifest manifest;

    public ManifestController(Manifest manifest) {
        this.manifest = manifest;
    }

    @GetMapping("/manifest.json")
    @CrossOrigin
    public Manifest produceManifest() {
        return manifest;
    }
}
