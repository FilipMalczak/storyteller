package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.SubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Closures.threadClosure;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.getCommands;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Rgx.RUN_MESSAGE;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.safeguard;

@Value
@Slf4j
public class Thread implements SubEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<ThreadClosure> body;

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var cmds = getCommands(this, workspace, manager);
        var workingCopy = cmds.getWorkingCopy();
        var knownProgress = cmds.initializeSequenceProgress();
        cmds.startSequence();
        var knownCommmits = workingCopy.gitLog(
                workingCopy.getTagCommitId(
                    workingCopy.getTag(
                            buildRefName(episodeId, START)
                        )
                        .get()
                ),
                workingCopy.getBranch(
                        buildRefName(episodeId, PROGRESS)
                    )
                    .get()
                    .getObjectId()
            );
        var sceneLikeCommits = knownCommmits.stream()
            .filter(rc -> rc.getFullMessage().matches(RUN_MESSAGE))
            .toList();
        body.action(threadClosure(knownProgress, sceneLikeCommits, episodeId, workspace, manager));

        cmds.safeguardOnProgressHead();
//        assertLastTagIsEndTag(); //todo implement when you fix safeguardSingleParentWithTag
        cmds.endSequence();
        cmds.integrate();
    }
}
