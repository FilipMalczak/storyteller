package com.github.filipmalczak.storyteller.impl.jgit.episodes.structure;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.Episode;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.EpisodeId;

public interface SubEpisode extends Episode {
    EpisodeId getParentId();
}
