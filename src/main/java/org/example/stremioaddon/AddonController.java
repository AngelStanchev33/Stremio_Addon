package org.example.stremioaddon;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AddonController {

    Manifest manifest = new Manifest();

    @GetMapping("/manifest.json")
    @CrossOrigin
    public Manifest produceManifest() {
        System.out.println(manifest);
        System.out.println();

        return manifest;
    }

    @GetMapping("/subtitles/{type}/{id}/{extra}.json")
    @CrossOrigin
    public Subtitles getSubtitles(@PathVariable String type,
                                  @PathVariable String id,
                                  @PathVariable String extra){

        Subtitles subtitles = new Subtitles();

        System.out.println("HIT SUBTITLES");
        return subtitles;
    }


}
