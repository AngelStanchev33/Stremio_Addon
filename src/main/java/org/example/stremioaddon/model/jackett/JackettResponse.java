package org.example.stremioaddon.model.jackett;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JackettResponse {

    @JsonProperty("Title")
    private String title;

    @JsonProperty("MagnetUri")
    private String url;

    @JsonProperty("InfoHash")
    private String infoHash;

    @JsonProperty("Size")
    private Long size;

    @JsonProperty("Seeders")
    private String seeders;

    @JsonProperty("CategoryDesc")
    private String category;

}
