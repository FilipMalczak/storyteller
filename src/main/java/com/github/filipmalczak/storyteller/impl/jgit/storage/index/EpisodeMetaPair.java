package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import lombok.NonNull;

public class EpisodeMetaPair extends Pair<EpisodeId, EpisodeSpec> {
    public EpisodeMetaPair(@NonNull EpisodeId first, @NonNull EpisodeSpec second) {
        super(first, second);
    }
}
