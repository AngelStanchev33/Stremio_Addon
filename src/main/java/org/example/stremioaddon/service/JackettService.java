package org.example.stremioaddon.service;

import org.example.stremioaddon.config.JacketProperties;
import org.example.stremioaddon.model.jackett.JackResponseWrapper;
import org.example.stremioaddon.model.jackett.JackettResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JackettService {

    private final Logger logger = LoggerFactory.getLogger(JackettService.class);
    private final JacketProperties jacketProperties;
    private final RestTemplate restTemplate;

    public JackettService(JacketProperties jacketProperties, RestTemplate restTemplate) {
        this.jacketProperties = jacketProperties;

        this.restTemplate = restTemplate;
    }

    public JackResponseWrapper findSteams(String title, String year, String category) {
        String tileAndYear = title + " " + year;
        String queryParam = tileAndYear.replace(" ", "+");

        String url = jacketProperties.getBaseUrl()
                + jacketProperties.getPATH()
                + "?apikey=" + jacketProperties.getApiKey()
                + "&Query=" + queryParam
                + "&CategoryDesc=" + category;

        logger.debug("URL with + encoding: {}", url);

        JackResponseWrapper response = restTemplate.getForObject(url, JackResponseWrapper.class);
        assert response != null;
        logger.debug("Results: {}", (response.getResult() != null ? response.getResult().size() : 0));


        JackResponseWrapper filteredResponse = filter(response, year, category);
        logger.debug("Filtering....");
        logger.debug("{} Matches found", filteredResponse.getResult().size());

        return filteredResponse;
    }


    public static JackResponseWrapper filter(JackResponseWrapper jackResponseWrapper,
                                             String year,
                                             String category) {
        List<JackettResponse> filtered = jackResponseWrapper
                .getResult()
                .stream()
                .filter(r -> r.getTitle().contains(year))
                .filter(r -> r.getCategory().startsWith(category))
                .collect(Collectors.toList());

        jackResponseWrapper.setResult(filtered);
        return jackResponseWrapper;
    }
}
