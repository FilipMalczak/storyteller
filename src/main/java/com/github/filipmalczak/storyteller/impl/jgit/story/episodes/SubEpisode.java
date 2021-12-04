package com.github.filipmalczak.storyteller.impl.jgit.story.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.story.Episode;
import com.github.filipmalczak.storyteller.impl.jgit.story.EpisodeId;

public interface SubEpisode extends Episode {
    EpisodeId getParentId();
}
