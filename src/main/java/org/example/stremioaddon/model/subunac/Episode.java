package org.example.stremioaddon.model.subunac;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Episode extends Video {

    private Integer season;
    private Integer episode;

    public Episode(String title, String type, String year, Integer season, Integer episode) {
        super(title, type, year);
        this.season = season;
        this.episode = episode;
    }
}
