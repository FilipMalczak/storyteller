package com.github.filipmalczak.storyteller.impl.jgit.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.NonNull;

//todo seal the interface
public interface Episode {

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

}
