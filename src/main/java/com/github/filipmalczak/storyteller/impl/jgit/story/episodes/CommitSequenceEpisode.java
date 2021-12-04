package com.github.filipmalczak.storyteller.impl.jgit.story.episodes;

import com.github.filipmalczak.storyteller.impl.jgit.story.Episode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;

import java.util.List;

public interface CommitSequenceEpisode extends Episode  {
    List<ObjectId> getCommits(Git git);
}
