package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.DecisionClosure;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.SubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.SneakyThrows;
import lombok.Value;

import java.util.LinkedList;

import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Closures.arcClosure;
import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;

@Value
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


    @SneakyThrows
    @Override
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var cmds = getCommands(this, workspace, manager);
        var knownProgress = new LinkedList<>(cmds.initializeSequenceProgress());
        cmds.startSequence();
        body.action(arcClosure(knownProgress, episodeId, workspace, manager));
        cmds.getWorkingCopy().safeguardOnBranchHead(buildRefName(episodeId, PROGRESS));
        //todo assert current commit is integration commit
        cmds.endSequence();
        cmds.integrate();

    }
}
