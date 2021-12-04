package com.github.filipmalczak.storyteller.impl.jgit.story.indexing;

import com.github.filipmalczak.storyteller.impl.jgit.storage.index.EpisodeMetaPair;
import com.github.filipmalczak.storyteller.impl.jgit.story.Episode;
import com.github.filipmalczak.storyteller.impl.jgit.story.EpisodeId;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
@Value
@Accessors(chain = true)
public class EpisodeIndex<T extends Episode> {
    EpisodeId scopeId;

    //stack[0] - top level episode = story; last element is parent of currentId; stories have empty stack, first arc has only the story
    List<EpisodeId> episodeStack;

    List<EpisodeMetaPair> childEpisodes;
}
