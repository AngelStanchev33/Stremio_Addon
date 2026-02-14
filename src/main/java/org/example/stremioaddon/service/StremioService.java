package org.example.stremioaddon.service;

import org.example.stremioaddon.model.reponse.SubtitleResponse;
import org.example.stremioaddon.model.reponse.SubtitleWrapper;
import org.example.stremioaddon.model.subunac.SubsUnacsSubtitle;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StremioService {

    public SubtitleWrapper mapSubsToStremioStandard(Map<String, SubsUnacsSubtitle> scrappedSubs) {
        SubtitleWrapper subtitleWrapper = new SubtitleWrapper();

        for (SubsUnacsSubtitle sub : scrappedSubs.values()) {
            SubtitleResponse stremioSubs = new SubtitleResponse();

            stremioSubs
                    .setId(sub.getId())
                    .setUrl(sub.getLink())
                    .setLang("Bulgarian");


            subtitleWrapper.getSubtitles()
                    .add(stremioSubs);

        }

        return subtitleWrapper;
    }
}
