package com.github.filipmalczak.storyteller.impl.jgit.story;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.TagBasedSubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.story.indexing.EpisodeSpec;
import lombok.SneakyThrows;
import lombok.Value;

import static com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata.buildMetadata;
import static com.github.filipmalczak.storyteller.impl.jgit.story.RefNames.*;
import static java.util.Arrays.asList;

@Value
public class Arc implements TagBasedSubEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<ArcClosure> body;

    @SneakyThrows
    @Override
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var workingCopy = manager.open(workspace);
        //todo if branch exists, checkout it, store its index; rename to ...-restarted-on-<now>; create new such branch; pass the index to the closure, so it can reconcile as it goes
        var history = workingCopy.resolveProgress(episodeId);
        var repo = workingCopy.getRepository();
        var startTagName = buildRefName(episodeId, START);
        if (workingCopy.tagExists(startTagName)){
            workingCopy.checkoutExisting(startTagName);
            workingCopy.safeguardValidIndexFile(episodeId);
            workingCopy.safeguardSingleParentWithTag("whatever"); //todo tagName is ignored; see assertLastTagIsEndTag below
        } else {
            workingCopy
                .getIndexFile()
                .setMetadata(
                    buildMetadata(
                        episodeId,
                        parentId,
                        EpisodeSpec.builder()
                            .type(EpisodeType.ARC)
                            .name(name)
                            .build()
                    )
                );
            workingCopy.commit(episodeId.toString());
            workingCopy.createTag(startTagName);;
            workingCopy.push(asList(buildRefName(episodeId, PROGRESS)), true);
        }
        body.action(new MergeUpClosure(episodeId, history, workspace, manager));
        workingCopy.safeguardOnBranchHead(buildRefName(episodeId, PROGRESS));
//        assertLastTagIsEndTag(); //todo implement when you fix safeguardSingleParentWithTag
        var endTagName = buildRefName(episodeId, END);
        if (workingCopy.tagExists(endTagName)){
            repo.checkout().setName(endTagName).call();
            workingCopy.safeguardValidIndexFile(episodeId);
//            assertValidTagOnParentCommit(); //todo ditto
        } else {
            workingCopy
                .getIndexFile()
                .setMetadata(
                    buildMetadata(
                        episodeId,
                        parentId,
                        EpisodeSpec.builder()
                            .type(EpisodeType.ARC)
                            .name(name)
                            .build()
                    )
                );
            workingCopy.commit(episodeId.toString());
            workingCopy.createTag(endTagName);
            workingCopy.push(asList(buildRefName(episodeId, PROGRESS)), true);
        }
    }
}
