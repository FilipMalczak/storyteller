package com.github.filipmalczak.storyteller.impl.jgit.story;

import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.CommitSequenceEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.TagBasedSubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.story.indexing.EpisodeSpec;
import lombok.SneakyThrows;
import lombok.Value;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;

import java.util.List;

import static com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata.buildMetadata;
import static com.github.filipmalczak.storyteller.impl.jgit.story.Episode.buildRefName;
import static java.util.Arrays.asList;

@Value
public class Thread implements TagBasedSubEpisode, CommitSequenceEpisode {
    EpisodeId episodeId;
    String name;
    EpisodeId parentId;
    ActionBody<ThreadClosure> body;
    List<Ref> commitRefs;

    @Override
    @SneakyThrows
    public void tell(Workspace workspace, DiskSpaceManager manager) {
        var workingCopy = manager.open(workspace);
        //todo similar as with arc
        var history = workingCopy.resolveProgress(episodeId);
        var repo = workingCopy.getRepository();
        var start = getStartEpisode(repo);
        if (start.isPresent()){
            workingCopy.checkoutExisting(start.get().getName());
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
            workingCopy.createTag(getStartEpisodeName());;
            workingCopy.push(asList(buildRefName(episodeId, "progress")), true);
        }
        body.action(new MergeUpClosure(episodeId, history, workspace, manager));
        workingCopy.safeguardOnBranchHead(buildRefName(episodeId, "progress"));
//        assertLastTagIsEndTag();//todo
        var end = getEndEpisode(repo);
        if (end.isPresent()){
            repo.checkout().setName(start.get().getName()).call();
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
            workingCopy.createTag(getEndEpisodeName());;
            //todo this specific call can be realized with currentId() and wrapped into "pushSequenceEvent"
            workingCopy.push(asList(buildRefName(episodeId, "progress")), true);
        }
    }

    @Override
    public List<ObjectId> getCommits(Git git) {
        return commitRefs.stream().map(r -> r.getObjectId()).toList();
    }
}
