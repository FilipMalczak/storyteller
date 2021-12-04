package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import com.github.filipmalczak.storyteller.impl.jgit.story.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.story.indexing.EpisodeSpec;
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
    List<EpisodeMetaPair> orderedIndex = new LinkedList<>();

    public static Metadata buildMetadata(@NonNull EpisodeId id, EpisodeId parentId, @NonNull EpisodeSpec spec) {
        return Metadata.builder()
            .currentId(id)
            .parentId(parentId)
            .currentSpec(spec)
            .build();
    }
}
