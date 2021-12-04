package com.github.filipmalczak.storyteller.impl.jgit.story.indexing;

import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
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
