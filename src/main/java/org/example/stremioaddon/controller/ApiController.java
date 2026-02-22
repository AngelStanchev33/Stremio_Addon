package org.example.stremioaddon.controller;

import org.example.stremioaddon.model.jackett.JackResponseWrapper;
import org.example.stremioaddon.model.ombd.OmdbResponse;
import org.example.stremioaddon.service.JackettService;
import org.example.stremioaddon.service.OmdbService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class ApiController {
    private final JackettService jackettService;
    private final OmdbService omdbService;

    public ApiController(JackettService jackettService, OmdbService omdbService) {
        this.jackettService = jackettService;
        this.omdbService = omdbService;
    }

    @GetMapping("/jackett")
    public JackResponseWrapper jacketTest() {
        return jackettService.findSteams("How to train your dragon", "2025", "Movies");
    }

    @GetMapping ("/ombd")
    public OmdbResponse ombdTest(){
        return omdbService.getByImdbId("tt0468569");
    }

}
