package com.github.filipmalczak.storyteller.impl.jgit.story.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.story.Episode;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import java.util.Optional;

public interface TagBasedEpisode extends Episode {
    /**
     * Tag; marks  commit when this episode was put on top of the stack
     */
    @SneakyThrows
    default Optional<Ref> getStartEpisode(Git currentWorkspace) {
        return currentWorkspace.tagList().call().stream().filter(r -> r.getName().equals("refs/tags/"+getStartEpisodeName())).findAny();
    }

    default String getStartEpisodeName(){
        return getRefName("start");
    }

    /**
     * Tag; marks last commit when this episode was at the top of the stack
     */
    @SneakyThrows
    default Optional<Ref> getEndEpisode(Git currentWorkspace) {
        return currentWorkspace.tagList().call().stream().filter(r -> r.getName().equals("refs/tags/"+getEndEpisodeName())).findAny();
    }

    default String getEndEpisodeName(){
        return getRefName("end");
    }
}
