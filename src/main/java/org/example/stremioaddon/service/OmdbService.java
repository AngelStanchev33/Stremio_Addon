package org.example.stremioaddon.service;

import org.example.stremioaddon.config.OmdbProperties;
import org.example.stremioaddon.model.reponse.OmdbResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class OmdbService {
    private final OmdbProperties omdbProperties;
    private final RestTemplate restTemplate;

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

        return restTemplate.getForObject(url, OmdbResponse.class);
    }


}
