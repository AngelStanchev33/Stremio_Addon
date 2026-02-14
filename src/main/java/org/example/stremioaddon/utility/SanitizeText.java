package org.example.stremioaddon.utility;

import org.springframework.stereotype.Component;

@Component
public class SanitizeText {
    public String sanitize(String text) {
        if (text == null) return "";
        return text.replaceAll("[']", "").trim();
    }
}
