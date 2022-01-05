package com.github.filipmalczak.storyteller.impl.jgit.episodes.identity;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EpisodeSpec {
    EpisodeType type;
    String name;
    @Singular("meta")
    Map<String, Object> metadata = new HashMap<>();
}