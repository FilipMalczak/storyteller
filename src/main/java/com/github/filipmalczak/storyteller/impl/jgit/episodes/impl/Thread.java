package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.TaleContext;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.DefineAndRun;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.ExecuteRoot;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.ExecuteSequence;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.SubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.getCommands;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Rgx.RUN_MESSAGE;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;

@Value
@Slf4j
public class Thread implements SubEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<ThreadClosure> body;

    protected static ThreadClosure closure(EpisodeId parentId, TaleContext context){
        AtomicInteger childCount = new AtomicInteger(0);
        //dont replace with lambda; this version is more readable (next comment disables IntelliJ suggestion)
        //noinspection Convert2Lambda
        return new ThreadClosure() {
            @Override
            public void scene(String name, ActionBody<Storage> body) {
                DefineAndRun.builder()
                    .scope(parentId)
                    .episodeInScopeIdx(childCount.getAndIncrement())
                    .toDefine(EpisodeSpec.builder().type(EpisodeType.SCENE).name(name).build())
                    .toRun(body)
                    .build()
                    .run(context.getWorkingCopy());
            }
        };
    }

    @Override
    @SneakyThrows
    public void tell(@NonNull TaleContext context) {
        var workingCopy = context.getWorkingCopy();
        invariant(
            buildRefName(parentId, PROGRESS).equals(workingCopy.head()),
            "" //todo name invariant
        );
        ExecuteSequence.builder()
            .sequenceId(episodeId)
            .parentId(parentId)
            .exec(() ->
                body.action(closure(episodeId, context))
            )
            .build()
            .run(workingCopy);
//        log.info("Thread "+episodeId+" ("+getName()+") start");
//        var cmds = getCommands(this, context.getWorkspace(), context.getManager());
//        var workingCopy = cmds.getWorkingCopy();
//        var knownProgress = cmds.initializeSequenceProgress();
//        log.info("Thread "+episodeId+" ("+getName()+") known progress: "+knownProgress);
//        cmds.startSequence();
//        log.info("Thread "+episodeId+" ("+getName()+") started seq");
//        var knownCommmits = workingCopy.gitLog(
//                workingCopy.getTagCommitId(
//                    workingCopy.getTag(
//                            buildRefName(episodeId, START)
//                        )
//                        .get()
//                ),
//                workingCopy.getBranch(
//                        buildRefName(episodeId, PROGRESS)
//                    )
//                    .get()
//                    .getObjectId()
//            );
//        log.info("Thread "+episodeId+" ("+getName()+") known commits: "+knownCommmits);
//        var sceneLikeCommits = knownCommmits.stream()
//            .filter(rc -> rc.getFullMessage().matches(RUN_MESSAGE))
//            .toList();
//        log.info("Thread "+episodeId+" ("+getName()+") scene likes: "+knownCommmits);
//        body.action(threadClosure(knownProgress, sceneLikeCommits, episodeId, context.getWorkspace(), context.getManager(), cmds::define));
//
//        cmds.safeguardOnProgressHead();
////        assertLastTagIsEndTag(); //todo implement when you fix safeguardSingleParentWithTag
//        log.info("Thread "+episodeId+" ("+getName()+") end seq");
//        cmds.endSequence();
//        log.info("Thread "+episodeId+" ("+getName()+") integrate");
//        cmds.integrate();
//        log.info("Thread "+episodeId+" ("+getName()+") end");
    }
}
