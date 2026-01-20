package org.example.stremioaddon;

import java.util.ArrayList;
import java.util.List;

public class Subtitles {

    List<Pair> subtitles = new ArrayList<>();

    public void add(String id, String lang, String url) {
        subtitles.add(new Pair(lang, url, id));
    }

    public static class Pair {

        String id;
        String lang;
        String url;

        public Pair(String id, String lang, String url) {
            this.id = id;
            this.lang = lang;
            this.url = url;
        }

        public String getLang() {
            return lang;
        }

        public String getUrl() {
            return url;
        }
    }
}


