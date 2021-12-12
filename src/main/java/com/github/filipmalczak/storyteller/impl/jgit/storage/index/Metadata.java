package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Data
@Setter(AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    @NonNull EpisodeId currentId;
    EpisodeId parentId;
    @NonNull EpisodeSpec currentSpec;

    @Singular("subEpisode")
    List<EpisodeDefinition> orderedIndex = new LinkedList<>();

    public static Metadata buildMetadata(@NonNull EpisodeDefinition definition, EpisodeId parentId) {
        return Metadata.builder()
            .currentId(definition.getEpisodeId())
            .parentId(parentId)
            .currentSpec(definition.getEpisodeSpec())
            .build();
    }

    EpisodeDefinition toDefinition(){
        return new EpisodeDefinition(currentId, currentSpec);
    }
}
