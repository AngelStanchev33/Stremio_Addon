package org.example.stremioaddon.model.stremio;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleWrapper {
    Set<SubtitleResponse> subtitles = new HashSet<>();
    private Integer cacheMaxAge;
    private Integer staleRevalidate;
    private Integer staleError;
}
