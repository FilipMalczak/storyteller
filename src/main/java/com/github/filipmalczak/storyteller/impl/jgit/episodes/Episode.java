package com.github.filipmalczak.storyteller.impl.jgit.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.NonNull;

import java.util.Map;

import static java.util.Collections.emptyMap;

//todo seal the interface
public interface Episode {

    EpisodeId getEpisodeId();
    String getName();

    /*todo these belong in returned types; e.g. EpisodeType::typeOf(clazz) ro EpisodeSpec::specOf(episode)*/
    static EpisodeType getEpisodeType(Class<? extends Episode> clazz){
        return EpisodeType.findByBackend(clazz).get();
    }

    static EpisodeSpec getEpisodeSpec(Episode episode){
        return getEpisodeSpec(episode, emptyMap());
    }

    static EpisodeSpec getEpisodeSpec(Episode episode, Map<String, Object> metadata){
        return new EpisodeSpec(episode.getEpisodeId().getType(), episode.getName(), metadata);
    }

    static EpisodeDefinition getEpisodeDefinition(Episode episode){
        return getEpisodeDefinition(episode, emptyMap());
    }

    static EpisodeDefinition getEpisodeDefinition(Episode episode, Map<String, Object> metadata){
        return new EpisodeDefinition(episode.getEpisodeId(), getEpisodeSpec(episode, metadata));
    }

    /**
     *  @param workspace use this to run this episode; usually pass that to subepisodes, which makes them run one by one
     * @param manager use this if you want to manually request a workspace , e,g multithread and use workspace per subepisode
     */
    void tell(@NonNull Workspace workspace, @NonNull DiskSpaceManager manager);

}
