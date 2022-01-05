package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.TaleContext;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.ExecuteLeaf;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StageBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.LeafEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.data.DirectoryStorage;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;

@Value
@Slf4j
public class Scene implements LeafEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<Storage> body;


    @Override
    @SneakyThrows
    public void tell(@NonNull TaleContext context) {
        log.info("Scene "+episodeId+" ("+getName()+") start");
//        var cmds = getCommands(this, context.getWorkspace(), context.getManager());
        var workingCopy = context.getWorkingCopy();
        invariant(
            buildRefName(parentId, PROGRESS).equals(workingCopy.head()),
            "" //todo name invariant
        );
        var storage = new DirectoryStorage(context.getWorkspace().getWorkingDir());
        log.info("Using storage: %s",storage);
        ExecuteLeaf.builder()
            .parentId(parentId)
            .leafId(episodeId)
            .body(StageBody.runAction(body, storage))
            .build()
            .run(workingCopy);
//        workingCopy.checkoutExisting(buildRefName(parentId, PROGRESS));
//        workingCopy
//            .getIndexFile()
//            .setMetadata(
//                Metadata.buildMetadata(
//                    cmds.getDefinition(),
//                    parentId
//                )
//            );
//        log.info("Scene "+episodeId+" ("+getName()+") metadata up to date");
//        var storage = new DirectoryStorage(context.getWorkspace().getWorkingDir());
//        body.action(storage);
//        log.info("Scene "+episodeId+" ("+getName()+") commiting");
//        workingCopy.commit(buildRefName(parentId, PROGRESS), episodeId.toString());
//        log.info("Scene "+episodeId+" ("+getName()+") commited");
//        workingCopy.push(asList(buildRefName(parentId, PROGRESS)), false);
//        log.info("Scene "+episodeId+" ("+getName()+") pushed; end scene");

    }
}
