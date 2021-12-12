package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.DecisionClosure;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.RootEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.LinkedList;

import static com.github.filipmalczak.storyteller.api.story.ToBeContinuedException.toBeContinued;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Closures.arcClosure;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;

@Value
public class Story implements RootEpisode {
    EpisodeId episodeId;
    String name;
    ActionBody<ArcClosure> body;

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var cmds = getCommands(this, workspace, manager);
        var knownProgress = new LinkedList<>(cmds.initializeSequenceProgress());
        cmds.startSequence();
        body.action(arcClosure(knownProgress, episodeId, workspace, manager));
        //todo assert current commit is integration commit
        //todo right now it finishes with last story child in index
        toBeContinued(cmds.getWorkingCopy()::pushAll); //todo are really all stories unfinished?
    }
}
