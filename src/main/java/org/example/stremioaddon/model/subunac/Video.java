package org.example.stremioaddon.model.subunac;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Video {

    private String title;
    private String type;
    private String year;

    public Video(String title, String type, String year) {
        this.title = title;
        this.type = type;
        this.year = year;
    }
}