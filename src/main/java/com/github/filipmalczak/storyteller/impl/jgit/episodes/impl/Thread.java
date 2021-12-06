package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.MergeUpClosure;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.indexing.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.structure.EpisodeNode;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.structure.LeafSequence;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import lombok.SneakyThrows;
import lombok.Value;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.util.List;

import static com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata.buildMetadata;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static java.util.Arrays.asList;

@Value
public class Thread implements EpisodeNode, LeafSequence {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<ThreadClosure> body;
    List<Ref> commitRefs; //todo unused?

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var workingCopy = manager.open(workspace);
        //todo similar as with arc
        var history = workingCopy.resolveProgress(episodeId);
        var repo = workingCopy.getRepository();
        var startTagName = buildRefName(episodeId, START);
        if (workingCopy.tagExists(startTagName)){
            workingCopy.checkoutExisting(startTagName);
            workingCopy.safeguardValidIndexFile(episodeId);
//            assertValidTagOnParentCommit();//todo
        } else {
            workingCopy
                .getIndexFile()
                .setMetadata(
                    buildMetadata(
                        episodeId,
                        parentId,
                        EpisodeSpec.builder()
                            .type(EpisodeType.THREAD)
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
//        assertLastTagIsEndTag();//todo
        var endTagName = buildRefName(episodeId, END);
        if (workingCopy.tagExists(endTagName)){
            repo.checkout().setName(endTagName).call();
            workingCopy.safeguardValidIndexFile(episodeId);
//            assertValidTagOnParentCommit();//todo
        } else {
            workingCopy
                .getIndexFile()
                .setMetadata(
                    buildMetadata(
                        episodeId,
                        parentId,
                        EpisodeSpec.builder()
                            .type(EpisodeType.THREAD)
                            .name(name)
                            .build()
                    )
                );
            workingCopy.commit(episodeId.toString());
            workingCopy.createTag(endTagName);;
            //todo this specific call can be realized with currentId() and wrapped into "pushSequenceEvent"
            workingCopy.push(asList(buildRefName(episodeId, PROGRESS)), true);
        }
    }
}
