package org.example.stremioaddon.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "omdb")
@Getter
@Setter
public class OmdbProperties {
    private String apiKey;
    private String baseUrl;
}
