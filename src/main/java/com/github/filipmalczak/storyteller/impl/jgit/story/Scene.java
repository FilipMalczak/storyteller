package com.github.filipmalczak.storyteller.impl.jgit.story;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.data.DirectoryStorage;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.SubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.story.indexing.EpisodeSpec;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata.buildMetadata;
import static com.github.filipmalczak.storyteller.impl.jgit.story.Episode.buildRefName;
import static java.util.Arrays.asList;

@Value
@Slf4j
public class Scene implements SubEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<Storage> body;

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var workingCopy = manager.open(workspace);
        workingCopy.checkoutExisting(buildRefName(parentId, "progress"));
//        workingCopy
//            .getIndexFile()
//            .setMetadata(
//                buildMetadata(
//                    episodeId,
//                    parentId,
//                    EpisodeSpec.builder()
//                        .type(EpisodeType.SCENE)
//                        .name(name)
//                        .build()
//                )
//            );
//        workingCopy.commit("Define "+episodeId.toString());
//        workingCopy.push(asList(buildRefName(parentId, "progress")), false);
        var storage = new DirectoryStorage(workspace.getWorkingDir());
        body.action(storage);
        workingCopy.commit(episodeId.toString());
        workingCopy.push(asList(buildRefName(parentId, "progress")), false);

    }

    private static String relativeTo(File descendant, File ancestor){
        //https://www.programiz.com/java-programming/examples/get-relative-path
        return ancestor.toURI().relativize(descendant.toURI()).getPath();
    }
}
