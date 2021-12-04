package com.github.filipmalczak.storyteller.impl.jgit.story;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.api.story.ArcClosure;
import com.github.filipmalczak.storyteller.api.story.DecisionClosure;
import com.github.filipmalczak.storyteller.api.story.ThreadClosure;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.EpisodeMetaPair;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.IndexFile;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.story.episodes.TagBasedSubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.story.indexing.EpisodeSpec;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.MergeCommand;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.jgit.story.Episode.buildRefName;
import static java.util.Arrays.asList;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
class MergeUpClosure implements ArcClosure, ThreadClosure {
    @NonNull EpisodeId parentId;
    @NonNull List<EpisodeMetaPair> history;
    @NonNull Workspace workspace;
    @NonNull DiskSpaceManager diskSpaceManager;

    @Getter List<Episode> episodes = new LinkedList<>();

    private Episode add(Episode episode){
        var workingCopy = diskSpaceManager.open(workspace);
        var parentProgressBranchName = buildRefName(parentId, "progress");
        var parentBranchExists = workingCopy.branchExists(parentProgressBranchName);
        if (!parentBranchExists) {
            throw new RuntimeException(); //fixme how did we even start a scene without a thread?
        }
        workingCopy.checkoutExisting(parentProgressBranchName);
        workingCopy
            .getIndexFile()
            .updateMetadata(m ->
                m.toBuilder()
                    .subEpisode(
                        new EpisodeMetaPair(
                            episode.getEpisodeId(),
                            new EpisodeSpec(
                                episode.getEpisodeType(),
                                episode.getName(),
                                new HashMap<>()
                            )
                        )
                    )
                    .build()
            );
        log.info("Commiting: Define episode "+episode.getEpisodeId()+" in index of "+parentId);
        workingCopy.commit(
            "Define episode "+episode.getEpisodeId()+" in index of "+parentId
        );
        workingCopy.push(asList(parentProgressBranchName), false);
        episodes.add(episode);
        return episode;
    }

    @SneakyThrows
    private void handle(Episode episode){

        add(episode);
        try {
            episode.tell(workspace, diskSpaceManager);
        } catch (RuntimeException e){
            //todo ???
            throw e;
        }
        if (episode instanceof TagBasedSubEpisode)
            mergeUp((TagBasedSubEpisode) episode);

    }

    @SneakyThrows
    private void mergeUp(TagBasedSubEpisode episode){
        //fixme basically copypasted to add(...)
        log.info("Merge up "+episode);
        var workingCopy = diskSpaceManager.open(workspace);
        var parentProgressBranchName = buildRefName(parentId, "progress");
        var parentBranchExists = workingCopy.branchExists(parentProgressBranchName);
        log.info("parent="+parentProgressBranchName+" exists: "+parentBranchExists);
        if (parentBranchExists){
            workingCopy.checkoutExisting(parentProgressBranchName);
            var metadata = workingCopy.getIndexFile().getMetadata();
            log.info("checkout ok");
            var tagName = buildRefName(episode.getEpisodeId(), "integrated-into", parentId);
            log.info("tag: "+tagName);
            if (workingCopy.tagExists(tagName)) {
                log.info("exists!");
//                workingCopy.checkoutExisting("/refs/heads/"+tagName);
                //todo perform validation - critical
                //  assert proper parents (one tag with x-end last on x-progress, the other y-start, previous on y-progress)
                //  assert proper message ??
            } else {
                log.info("gonna merge");
                workingCopy.getRepository()
                    .merge()
                    .include(workingCopy.getBranch(buildRefName(episode.getEpisodeId(), "progress")).get())
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    //todo message?
                    .call();
                workingCopy.getIndexFile().setMetadata(metadata);
                workingCopy.commit("Restore metadata");
                log.info("gonna tag");
                workingCopy.createTag(tagName);
                log.info("pushing "+parentProgressBranchName+" and tags");
                workingCopy.push(asList(parentProgressBranchName), true);
                log.info("here ya go");


            }
        } else {
            //todo ??? missing parent branch
            throw new RuntimeException();
        }
    }


    private EpisodeId resolveId(Optional<EpisodeMetaPair> meta, EpisodeType type, String name){
        if (meta.isEmpty())
            return EpisodeId.randomId(type);
        var metaVar = meta.get();
        if (metaVar.getFirst().getType().equals(type) &&
            metaVar.getSecond().getType().equals(type) &&
            metaVar.getSecond().getName().equals(name)){
            return metaVar.getFirst();
        }
        throw new RuntimeException(); //todo resolve conflicting expectations
    }

    @Override
    public void thread(String thread, ActionBody<ThreadClosure> body) {
        var expectedNext = history.stream().findFirst();
        var id = resolveId(expectedNext, EpisodeType.THREAD, thread); //todo this should take "resolver" parameter
        expectedNext.ifPresent(x -> history.remove(0));
        handle(new Thread(id, thread, parentId, body, new LinkedList<>()));
    }

    @Override
    public void arc(String arc, ActionBody<ArcClosure> body) {
        var expectedNext = history.stream().findFirst();
        var id = resolveId(expectedNext, EpisodeType.ARC, arc); //todo this should take "resolver" parameter
        expectedNext.ifPresent(x -> history.remove(0));
        handle(new Arc(EpisodeId.randomId(EpisodeType.ARC), arc, parentId, body));
    }

    @Override
    public <K> void decision(String decision, ActionBody<DecisionClosure<K>> body) {
//        var expectedNext = history.stream().findFirst();
//        var id = resolveId(expectedNext, EpisodeType.DECISION, decision); //todo this should take "resolver" parameter
//        expectedNext.ifPresent(x -> history.remove(0));
        throw new RuntimeException(); //todo implement me
    }

    @Override
    public void scene(String name, ActionBody<Storage> body) {
        //fixme !!! critical !!!
        handle(new Scene(EpisodeId.randomId(EpisodeType.SCENE), name, parentId, body));
    }
}
