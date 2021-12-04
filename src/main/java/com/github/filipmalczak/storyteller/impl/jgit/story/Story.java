package com.github.filipmalczak.storyteller.impl.jgit.story;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.IndexFile;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.TagBasedEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.story.indexing.EpisodeSpec;
import lombok.SneakyThrows;
import lombok.Value;

import static com.github.filipmalczak.storyteller.api.story.ToBeContinuedException.toBeContinued;
import static com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata.buildMetadata;
import static com.github.filipmalczak.storyteller.impl.jgit.story.Episode.buildRefName;
import static java.util.Arrays.asList;

@Value
public class Story implements TagBasedEpisode {
    EpisodeId episodeId;
    String name;
    ActionBody<ArcClosure> body;

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var workingCopy = manager.open(workspace);
        var repo = workingCopy.getRepository();
        var progress = workingCopy.resolveProgress(episodeId);
        var startTagName = buildRefName(episodeId, "start");
        var startTagExists = workingCopy.tagExists(startTagName);
        if (startTagExists){
            workingCopy.checkoutExisting("refs/tags/"+startTagName);
            workingCopy.safeguardValidIndexFile(episodeId);
//            assertParentEmptyTag(); //fixme
        } else {;
            workingCopy
                .getIndexFile()
                .setMetadata(
                    buildMetadata(
                        episodeId,
                        null,
                        EpisodeSpec.builder()
                            .type(EpisodeType.STORY)
                            .name(name)
                            .build()
                    )
                );
            workingCopy.commit(episodeId.toString());
//            workingCopy.commitPatterns("Started '"+ episodeId +"'", asList(workspace.indexFile().getAbsolutePath()));
            workingCopy.createTag(getStartEpisodeName());
            workingCopy.push(asList(), true);
        }
        body.action(new MergeUpClosure(episodeId, progress, workspace, manager));
        workingCopy.push(asList(buildRefName(episodeId, "progress")), false);
//        workingCopy.safeguardOnBranchHead(buildTagName(episodeId, "progress")); //todo
//        assertLastTagIsEndTag(); //fixme
        toBeContinued(); //todo are really all stories unfinished?
    }
}
