package com.github.filipmalczak.storyteller.impl.jgit.storage;

import com.github.filipmalczak.storyteller.impl.jgit.storage.index.EpisodeMetaPair;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.IndexFile;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import com.github.filipmalczak.storyteller.impl.jgit.story.EpisodeId;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.jgit.story.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.story.RefNames.buildRefName;
import static com.github.filipmalczak.storyteller.impl.jgit.story.Safeguards.safeguard;
import static java.util.Arrays.asList;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

@Value
@Slf4j
public class WorkingCopy {
    @NonNull Git repository;
    @NonNull Workspace workspace;

    @Getter(lazy = true) IndexFile indexFile = new IndexFile(workspace.getWorkingDir());

    @SneakyThrows
    public void checkoutExisting(String refish){
        getRepository().checkout().setName(refish).setCreateBranch(false).call();
    }

    @SneakyThrows
    public boolean branchExists(String branchName){
        return getBranch(branchName).isPresent();
    }

    @SneakyThrows
    public boolean tagExists(String tagName){
        //todo refactor along the lines of branchExists
        return getRepository().tagList().call().stream()
            .filter(r -> r.getName().equals("refs/tags/"+tagName))//todo it should be doable doing following isSYmbolic, maybe?
            .findAny().isPresent();
    }

    public void safeguardValidIndexFile(EpisodeId id){
        safeguard(
            getIndexFile().getMetadata().getCurrentId().equals(id),
            "current index file matches current episode ID"
        );
        safeguard(
            getIndexFile().getMetadata().getCurrentSpec().getType().equals(id.getType()),
            "current index file matches episode type internally"
        );
        //todo check parent ID (as in episode ID, not commit ID)
    }

    @SneakyThrows
    private List<RevCommit> gitLog(int maxCount){
        return stream(
                spliteratorUnknownSize(
                    repository.log()
                        .setMaxCount(maxCount)
                        .call().iterator(),
                    0
                ),
                false
            )
            .toList();
    }

    protected RevCommit currentCommit(){
        var log = gitLog(1);
        if (log.size() < 1)
            return null;
        return log.iterator().next();
    }

    @SneakyThrows
    protected RevCommit[] currentParents(){
        return currentCommit().getParents();
    }

    @SneakyThrows
    public void safeguardSingleParentWithTag(String tagNam1e){
        var current = currentCommit();
        safeguard(current.getParentCount() == 1, "current commit should have single parent");
        var parent = currentParents()[0];
        //todo safeguard that parent commit is head of parentId-progress
//        var tag = repository.tagList().call().stream().filter(t -> t.getObjectId().equals(parent.toObjectId())).findAny();
//        safeguard(tag.isPresent(), "tag '"+tagName+"' must exist and must point to current parent");
    }

    @SneakyThrows
    public List<EpisodeMetaPair> resolveProgress(EpisodeId id){
        var name = buildRefName(id, PROGRESS);
        if (branchExists(name)){
            checkoutExisting(name);
            Metadata latest = getIndexFile().getMetadata();
            return new LinkedList<>(latest.getOrderedIndex());
        }
        repository.checkout().setCreateBranch(true).setName(name).call();
        push(asList(name), false);
        return new LinkedList<>();
    }

    //todo remove 2nd arg
    @SneakyThrows
    public void commit(String msg){
        repository.add().addFilepattern(".").call();
        repository.add().setUpdate(true).addFilepattern(".").call();
        log.info("Add ./* called");
        var c = repository.commit().setMessage(msg).call();
        log.info("Commit: "+c);
    }

    @SneakyThrows
    public void createTag(String name){
        repository.tag().setName(name).call();
    }

    @SneakyThrows
    public void safeguardOnBranchHead(String name){
        var count = repository
            .branchList().call()
            .stream()
            .filter(r -> r.getName().equals("refs/heads/"+name))
            .filter(r -> r.getObjectId().equals(currentCommit().toObjectId()))
            .count();
        safeguard(
             count == 1,
            "current commit should be head of current progress branch (called '"+name+"')"
        );
    }

    @SneakyThrows
    public void push(List<String> branches, boolean tags){
        log.info("Push "+branches+" and "+(tags ? "" : "no ")+"tags");
        var cmd = repository.push()
            .setRefSpecs(branches.stream().map(b -> new RefSpec(b+":"+b)).toList());
        if (tags)
            cmd = cmd.setPushTags();
        cmd.call();
    }

    @SneakyThrows
    public Optional<Ref> getBranch(String name) {
        return getRepository().branchList().call().stream()
            .filter(r -> r.getName().equals("refs/heads/"+name))//todo it should be doable doing following isSYmbolic, maybe?
            .findAny();
    }
}
