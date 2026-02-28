package org.example.stremioaddon.model.yavka;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YavkaSubtitle {
    private String id;          // Unique identifier (link)
    private String filename;    // e.g., "Movie.Name.2012.srt"
    private String type;        // "movie" or "series"
    private String link;        // Download URL (subtitle page)
    private String title;       // Release title
    private String notes;       // Additional notes from tooltip
    private Integer year;       // Release year
    private Float fps;          // Frames per second
    private String uploader;    // Username who uploaded
}
