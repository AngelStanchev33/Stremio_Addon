package org.example.stremioaddon.utility;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TitleNormalization {

    private static final Map<String, String> TV_NAMING_FIXES = Map.of(
            "Marvel's Daredevil", "Daredevil",
            "Marvel's Luke Cage", "Luke Cage",
            "Marvel's Iron Fist", "Iron Fist",
            "DC's Legends of Tomorrow", "Legends of Tomorrow",
            "Doctor Who (2005)", "Doctor Who",
            "Star Trek: Deep Space Nine", "Star Trek DS9",
            "Star Trek: The Next Generation", "Star Trek TNG",
            "Superman & Lois", "Superman and Lois"
    );

    private static final Map<String, String> MOVIE_NAMING_FIXES = Map.of(
            "Back to the Future Part III", "Back to the Future 3",
            "Back to the Future Part II", "Back to the Future 2",
            "Bill & Ted Face the Music", "Bill Ted Face the Music"
    );

    //getOrDefault(key, defaultValue) значи:
    //
    //Ако title съществува като ключ в Map-а → върни новата стойност
    //
    //Ако title НЕ съществува → върни defaultValue (което е същия title)

    public String fixTvNaming(String title) {
        return TV_NAMING_FIXES.getOrDefault(title, title);
    }

    public String fixMovieNaming(String title) {
        return MOVIE_NAMING_FIXES.getOrDefault(title, title);
    }

}
