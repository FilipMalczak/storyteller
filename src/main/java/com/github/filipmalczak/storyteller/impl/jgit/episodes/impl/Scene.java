package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.LeafEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.data.DirectoryStorage;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util.Commands.getCommands;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static java.util.Arrays.asList;

@Value
@Slf4j
public class Scene implements LeafEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<Storage> body;


    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var cmds = getCommands(this, workspace, manager);
        var workingCopy = cmds.getWorkingCopy();
        workingCopy.checkoutExisting(buildRefName(parentId, PROGRESS));
        workingCopy
            .getIndexFile()
            .setMetadata(
                Metadata.buildMetadata(
                    cmds.getDefinition(),
                    parentId
                )
            );
        var storage = new DirectoryStorage(workspace.getWorkingDir());
        body.action(storage);
        workingCopy.commit(episodeId.toString());
        workingCopy.push(asList(buildRefName(parentId, PROGRESS)), false);

    }
}
