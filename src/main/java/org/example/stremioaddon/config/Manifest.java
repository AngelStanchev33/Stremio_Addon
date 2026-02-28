package org.example.stremioaddon.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Manifest {

    private final String id = "com.github.addon.angel33.sub";
    private final String version = "1.0.0";
    private final String name = "Angellio";
    private final String description = "Bulgarian steams & subtitles";
    private final String[] resources = {"stream", "subtitles"};
    private final String[] types = {"movie", "series"};
    private final String[] catalogs = {};
    private final String[] idPrefixes = {"tt"};
    private final String background = "";
    private final String logo = "https://icons.iconarchive.com/icons/wikipedia/flags/256/BG-Bulgaria-Flag-icon.png";
    private final String contactEmail = "";

}



