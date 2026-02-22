package org.example.stremioaddon.service;

import org.example.stremioaddon.config.OmdbProperties;
import org.example.stremioaddon.model.ombd.OmdbResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class OmdbService {
    private final OmdbProperties omdbProperties;
    private final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(OmdbService.class);

    public OmdbService(OmdbProperties omdbProperties, RestTemplate restTemplate) {
        this.omdbProperties = omdbProperties;
        this.restTemplate = restTemplate;
    }

    public OmdbResponse getByImdbId(String imdbId) {
        String url = UriComponentsBuilder
                .fromUri(URI.create(omdbProperties.getBaseUrl()))
                .queryParam("apikey", omdbProperties.getApiKey())
                .queryParam("i", imdbId)
                .toUriString();

        logger.debug("URL with + encoding: {}", url);
        return restTemplate.getForObject(url, OmdbResponse.class);
    }


    public String getType(String type) {

        if (type.equals("movie")) {
            return "Movies";
        } else {
            return null;
        }
    }

}
