package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.structure.EpisodeLeaf;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.data.DirectoryStorage;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static java.util.Arrays.asList;

@Value
@Slf4j
public class Scene implements EpisodeLeaf {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<Storage> body;

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var workingCopy = manager.open(workspace);
        workingCopy.checkoutExisting(buildRefName(parentId, PROGRESS));
        var storage = new DirectoryStorage(workspace.getWorkingDir());
        body.action(storage);
        workingCopy.commit(episodeId.toString());
        workingCopy.push(asList(buildRefName(parentId, PROGRESS)), false);

    }
}
