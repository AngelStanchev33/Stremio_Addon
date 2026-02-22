package org.example.stremioaddon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("jackett")
@Data
public class JacketProperties {
    private String apiKey;
    private String baseUrl;
    private final String PATH = "/api/v2.0/indexers/all/results";

}
