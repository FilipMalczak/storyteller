package com.github.filipmalczak.storyteller.impl.jgit.episodes.identity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDefinition {
    @NonNull EpisodeId episodeId;
    @NonNull EpisodeSpec episodeSpec;
}
