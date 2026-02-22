package org.example.stremioaddon.model.stremio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StreamResponse {

    @JsonProperty("name")
    //name of the stream; usually used for stream quality
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("url")
    private String url;

//    @JsonProperty("fileIdx")
//    private Integer fileIdx = 0;

//    @JsonProperty("infoHash")
//    private String infoHash;

//    @JsonProperty("sources")
//    private List<String> sources;
}
