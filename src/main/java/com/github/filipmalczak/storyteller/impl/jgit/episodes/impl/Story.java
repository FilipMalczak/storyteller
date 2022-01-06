package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.TaleContext;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.ExecuteSequence;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.DefinitionFactory;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StageBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StartPointFactory;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.RootEpisode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.flogger.Flogger;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.valid4j.Assertive.require;


@Value
@Flogger
public class Story implements RootEpisode {
    EpisodeId episodeId;
    String name;
    ActionBody<ArcClosure> body;

    @Override
    @SneakyThrows
    public void tell(@NonNull TaleContext context) {
        var workingCopy = context.getWorkingCopy();
        log.atFine().log("Head when starting to tell a %s story: %s", episodeId, workingCopy.head());
        require(
            buildRefName(episodeId, PROGRESS),
            equalTo(workingCopy.head())
        );
        ExecuteSequence.builder()
            .sequenceId(episodeId)
            .startPointFactory(StartPointFactory.constant("empty"))
            .definitionFactory(DefinitionFactory.constant(episodeId, name))
            .body(StageBody.runAction(body, Arc.closure(null, episodeId, context)))
            .build()
            .run(workingCopy);
//        log.atInfo().log("Story %s (%s) start", episodeId, name);
//        var cmds = getCommands(this, context.getWorkspace(), context.getManager());
//        cmds.getWorkingCopy().checkoutExisting("master");
//        var knownProgress = new LinkedList<>(cmds.initializeSequenceProgress());
//        log.atFine().log("Story %s known progress: %s", episodeId, knownProgress);
//        cmds.startSequence();
//        log.atFine().log("Story %s started sequence", episodeId);
//        body.action(arcClosure(knownProgress, episodeId, context.getWorkspace(), context.getManager(), cmds::define));
//        log.atFine().log("Story %s finished running", episodeId);
//        cmds.pushCurrentProgress(true);
//        log.atFine().log("Story %s pushed", episodeId);
//        //todo assert current commit is integration commit
//        //todo right now it finishes with last story child in index
//        log.atInfo().log("Story %s TBC", episodeId);
//        toBeContinued(cmds.getWorkingCopy()::pushAll); //todo are really all stories unfinished?
    }
}
