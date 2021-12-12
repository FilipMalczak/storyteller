package com.github.filipmalczak.storyteller.impl.jgit.episodes.tree;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.Episode;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;

public interface SubEpisode extends Episode {
    EpisodeId getParentId();
}
