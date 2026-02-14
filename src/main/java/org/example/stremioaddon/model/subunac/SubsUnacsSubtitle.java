package org.example.stremioaddon.model.subunac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SubsUnacsSubtitle {
    private String filename;
    private String type;
    private String link;

    public String getId() {
        return link;
    }


}