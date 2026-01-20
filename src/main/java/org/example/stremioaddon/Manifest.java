package org.example.stremioaddon;

public class Manifest {

    private final String id = "com.github.addon.angel33.sub";
    private final String version = "1.0.0";
    private final String name = "BgSubs";
    private final String description = "Bulgarian subtitles for movies and series";
    private final String[] resources = {"subtitles"};
    private final String[] types = {"movie", "series"};
    private final String[] catalogs = {};
    private final String[] idPrefixes = {};
    private final String background = "";
    private final String logo = "https://icons.iconarchive.com/icons/wikipedia/flags/256/BG-Bulgaria-Flag-icon.png";
    private final String contactEmail = "";

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getResources() {
        return resources;
    }

    public String[] getTypes() {
        return types;
    }

    public String[] getCatalogs() {
        return catalogs;
    }

    public String[] getIdPrefixes() {
        return idPrefixes;
    }

    public String getBackground() {
        return background;
    }

    public String getLogo() {
        return logo;
    }

    public String getContactEmail() {
        return contactEmail;
    }
}



