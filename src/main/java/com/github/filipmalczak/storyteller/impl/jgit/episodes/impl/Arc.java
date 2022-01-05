package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.DecisionClosure;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.TaleContext;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.DefineAndIntegrate;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.ExecuteSequence;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.DefinitionFactory;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StageBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StartPointFactory;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.SubEpisode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;

@Value
@Slf4j
public class Arc implements SubEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<ArcClosure> body;

//    public static Commands.ProgressLoader arcLoader(EpisodeId currentId) {
//        return  wc ->
//            wc.getIndexFile()
//                .getMetadata()
//                .getOrderedIndex()
//                .stream()
//                .map(d -> wc.getTag(buildRefName(currentId, INTEGRATE, d.getEpisodeId())).get());
//    }
//
//    protected Commands getCommands(Workspace workspace, DiskSpaceManager manager){
//        return new Commands(
//            parentId,
//            getEpisodeDefinition(this),
//            workspace,
//            manager
//        );
//    }

//    private EpisodeDefinition handleDefinition(Optional<EpisodeDefinition> expected, EpisodeSpec spec){
//        if (expected.isEmpty()){
//            var id = EpisodeId.randomId(spec.getType());
//            return new EpisodeDefinition(id, spec);
//        }
//        var exp = expected.get();
//        if (spec.equals(exp))
//            return exp;
//        throw new RuntimeException(); //todo this is the place where you can salvage some work
//    }

    protected static ArcClosure closure(EpisodeId parentId, EpisodeId episodeId, TaleContext context) {
        AtomicInteger childCount = new AtomicInteger(0);
        return new ArcClosure() {
            @Override
            public void thread(String thread, ActionBody<ThreadClosure> body) {
                DefineAndIntegrate.builder()
                    .scope(episodeId)
                    .episodeInScopeIdx(childCount.getAndIncrement())
                    .toDefine(EpisodeSpec.builder().type(EpisodeType.THREAD).name(thread).build())
                    .toRunFactory(id -> new Thread(id, thread, episodeId, body))
                    .context(context)
                    .build()
                    .run(context.getWorkingCopy());
            }

            @Override
            public void arc(String arc, ActionBody<ArcClosure> body) {
                DefineAndIntegrate.builder()
                    .scope(parentId)
                    .episodeInScopeIdx(childCount.getAndIncrement())
                    .toDefine(EpisodeSpec.builder().type(EpisodeType.ARC).name(arc).build())
                    .toRunFactory(id -> new Arc(id, arc, episodeId, body))
                    .context(context)
                    .build()
                    .run(context.getWorkingCopy());
            }


            @Override
            public <K> void decision(String decision, ActionBody<DecisionClosure<K>> body) {
                invariant(false, "Decisions are not implemented yet");
            }
        };
    }

    @SneakyThrows
    @Override
    public void tell(@NonNull TaleContext context) {
        var workingCopy = context.getWorkingCopy();
        invariant(
            buildRefName(episodeId, PROGRESS).equals(workingCopy.head()),
            "" //todo name invariant
        );
        ExecuteSequence.builder()
            .parentId(parentId)
            .sequenceId(episodeId)
            .startPointFactory(StartPointFactory.buildRef(DEFINE))
            .definitionFactory(DefinitionFactory.RETRIEVE_FROM_PARENT_INDEX)
            .body(StageBody.runAction(body, closure(parentId, episodeId, context)))
            .build()
            .run(workingCopy);
//        log.info("Arc "+episodeId+" ("+getName()+") start");
//        var cmds = getCommands(this, context.getWorkspace(), context.getManager());
//        var knownProgress = new LinkedList<>(cmds.initializeSequenceProgress());
//        log.info("Arc "+episodeId+" ("+getName()+") start sequence");
//        cmds.startSequence();
//        log.info("Arc "+episodeId+" ("+getName()+") started seq");
//        body.action(arcClosure(knownProgress, episodeId, context.getWorkspace(), context.getManager(), cmds::define));
//        cmds.getWorkingCopy().safeguardOnBranchHead(buildRefName(episodeId, PROGRESS));
//        //todo assert current commit is integration commit
//        log.info("Arc "+episodeId+" ("+getName()+") end seq");
//        cmds.endSequence();
//        log.info("Arc "+episodeId+" ("+getName()+") integrate");
//        cmds.integrate();
//        log.info("Arc "+episodeId+" ("+getName()+") end");

    }
}
