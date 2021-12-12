package com.github.filipmalczak.storyteller.impl.jgit.storage;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.IndexFile;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.safeguard;
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
        log.info("Checking out "+refish+"; reseting first");
        getRepository().reset().setRef("HEAD").call();
        log.info("actual checkout; current="+currentCommit());
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

    //todo move to utils?
    private static <T> Stream<T> toStream(Iterable<T> iterable){
        return stream(
            spliteratorUnknownSize(
                iterable.iterator(),
                0
            ),
            false
        );
    }

    @SneakyThrows
    private List<RevCommit> gitLog(int maxCount){
        return toStream(
            repository.log()
                .setMaxCount(maxCount)
                .call()
            )
            .toList();
    }

    @SneakyThrows
    public List<RevCommit> gitLog(ObjectId fromExc, ObjectId toInc){
        return toStream(
                repository.log()
                    .addRange(fromExc, toInc)
                    .call()
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

    //todo delete
//    @SneakyThrows
//    public List<EpisodeMetaPair> resolveProgress(EpisodeId id){
//        var name = buildRefName(id, PROGRESS);
//        if (branchExists(name)){
//            checkoutExisting(name);
//            Metadata latest = getIndexFile().getMetadata();
//            return new LinkedList<>(latest.getOrderedIndex());
//        }
//        repository.checkout().setCreateBranch(true).setName(name).call();
//        push(asList(name), false);
//        return new LinkedList<>();
//    }

    @SneakyThrows
    public void createBranch(String name){
        repository.branchCreate()
            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
            .setName(name)
            .call();
        push(asList(name), false);
    }

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
    public void pushAll(){
        log.info("push --all --tags");
        var cmd = repository.push()
            .setPushAll()
            .setPushTags();
        cmd.call();
    }

    @SneakyThrows
    public Optional<Ref> getBranch(String name) {
        return getRepository().branchList().call().stream()
            .filter(r -> r.getName().equals("refs/heads/"+name))//todo it should be doable doing following isSYmbolic, maybe?
            .findAny();
    }

    @SneakyThrows
    public Optional<Ref> getTag(String name) {
        return getRepository().tagList().call().stream()
            .filter(r -> r.getName().equals("refs/tags/"+name))//todo it should be doable doing following isSYmbolic, maybe?
            .findAny();
    }

    public <T> T withRevWalk(Function<RevWalk, T> body){
        try (var revWalk = new RevWalk(getRepository().getRepository())){
            var out = body.apply(revWalk);
            revWalk.dispose();
            return out;
        }
    }

    @SneakyThrows
    private static RevTag safeParseTag(RevWalk rw, Ref ref){
        return rw.parseTag(ref.getObjectId());
    }

    @SneakyThrows
    public ObjectId getTagCommitId(Ref tag){
        return withRevWalk(rw -> safeParseTag(rw, tag).getObject().getId());
    }
}
