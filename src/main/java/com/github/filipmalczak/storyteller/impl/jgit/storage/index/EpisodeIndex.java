package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;

@Value
@Accessors(chain = true)
public class EpisodeIndex {
    EpisodeId scopeId;

    //stack[0] - top level episode = story; last element is parent of currentId; stories have empty stack, first arc has only the story
    List<EpisodeId> episodeStack;

    List<EpisodeDefinition> childEpisodes;
}
