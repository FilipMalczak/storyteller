package com.github.filipmalczak.storyteller.impl.jgit.story;

import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import lombok.NonNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

//todo scene?
public interface Episode /*permits TagBasedEpisode, SubEpisode, TagBasedSubEpisode, Story, Arc, Decision, Research, StoryThread, Scene*/ {

    EpisodeId getEpisodeId();
    String getName();

    default EpisodeType getEpisodeType(){
        return EpisodeType.findByBackend(this.getClass()).get();
    }

    /**
     *  @param workspace use this to run this episode; usually pass that to subepisodes, which makes them run one by one
     * @param manager use this if you want to manually request a workspace , e,g multithread and use workspace per subepisode
     */
    void tell(@NonNull Workspace workspace, @NonNull DiskSpaceManager manager);


    /**
     * Utility for GIT-based episodes
     */
    default String getRefName(String marker){
        return buildRefName(getEpisodeId(), marker);
    }

    static String buildRefName(EpisodeId id, String domain, Object... subparts){
        return Stream.concat(
                Stream.of(id.toString(), domain),
                Stream.of(subparts).map(x -> x.toString())
            )
            .collect(Collectors.joining("/"));
    }
}
