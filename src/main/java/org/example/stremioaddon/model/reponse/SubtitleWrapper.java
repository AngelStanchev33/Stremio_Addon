package org.example.stremioaddon.model.reponse;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleWrapper {
    Set<SubtitleResponse> subtitles = new HashSet<>();
}
