package org.example.stremioaddon.model.stremio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamWrapper {
    private List<StreamResponse> streams = new ArrayList<>();
}
