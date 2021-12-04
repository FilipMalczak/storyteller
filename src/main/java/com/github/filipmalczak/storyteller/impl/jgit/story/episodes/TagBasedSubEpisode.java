package com.github.filipmalczak.storyteller.impl.jgit.story.episodes;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.jgit.story.Episode.buildRefName;

public interface TagBasedSubEpisode extends TagBasedEpisode, SubEpisode {
    /**
     * Tag for the merge commit of this episode to the parent
     */
    @SneakyThrows
    default Optional<Ref> getMergeUpEpisode(@NonNull Git currentWorkspace){
        return currentWorkspace.tagList().call().stream().filter(r -> r.getName().equals("refs/tags/"+this.getRefName("merged-to-"+getParentId()))).findAny();
    }

    default String getMergeUpEpisodeName(){
        return buildRefName(getParentId(), "merged-into-"+getParentId());
    }
}
