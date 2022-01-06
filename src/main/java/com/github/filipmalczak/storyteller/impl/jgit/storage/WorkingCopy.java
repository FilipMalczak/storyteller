package com.github.filipmalczak.storyteller.impl.jgit.storage;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.IndexFile;
import com.github.filipmalczak.storyteller.impl.jgit.utils.FSUtils;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.valid4j.Assertive.require;

@Value
@Slf4j
public class WorkingCopy {
    @NonNull Git repository;
    @NonNull Workspace workspace;

    @Getter(lazy = true) IndexFile indexFile = new IndexFile(workspace.getWorkingDir());

    public String head(){
        var f = new File(workspace.getWorkingDir(), ".git/HEAD");
        var txt = FSUtils.readFile(f);
        //todo perfect place for microoptimizations
        if (txt.startsWith("ref:")) {
            var parts = txt.split(":");
            var out  = parts[1].trim();
            if (out.startsWith("refs/heads/"))
                out = out.substring("refs/heads/".length());
            return out;
        }
        return txt;
    }

    @SneakyThrows
    public ObjectId resolveToCommitId(String ref){
//        Ref found = repository.getRepository().findRef(ref+"^{commit}");
//        Ref leaf = found.getLeaf();
//        log.debug("Resolution candidates: "+Stream.<Supplier<ObjectId>>of(
//            leaf::getPeeledObjectId,
//            leaf::getObjectId,
//            found::getPeeledObjectId,
//            found::getObjectId
//        ).map(Supplier::get).toList());
//        return Stream.<Supplier<ObjectId>>of(
//                leaf::getPeeledObjectId,
//                leaf::getObjectId,
//                found::getPeeledObjectId,
//                found::getObjectId
//            )
//            .map(x -> Optional.ofNullable(x.get()))
//            .filter(Optional::isPresent)
//            .map(Optional::get)
//            .findFirst()
//            .get();
        return repository.getRepository().resolve(ref+"^{commit}");
    }


    public void checkoutExisting(String refish){
        checkoutExisting(refish, false);
    }

    @SneakyThrows
    public void checkoutExisting(String refish, boolean pull){
        log.info("HEAD "+head());
        log.info("On branch "+ branchesWithHead(currentCommitId()));
        log.info("Checking out "+refish+"; then reseting");
        getRepository().checkout().setName(refish).setCreateBranch(false).call();
        getRepository().reset().setRef(refish).call();
        if (pull)
            getRepository().pull().call(); //this should pull current branch
    }

    public ObjectId currentCommitId(){
        return currentCommit().toObjectId();
    }

    @SneakyThrows
    private ObjectId safeResolve(String name){
        return getRepository().getRepository().resolve(name);
    }

    @SneakyThrows
    public List<String> branchesWithHead(ObjectId id){
        return getRepository().branchList().call()
            .stream()
            .<Optional<Ref>>map( r ->
                safeResolve(r.getName()).equals(id) ? Optional.of(r) : Optional.empty()
            )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Ref::getName)
            .toList();
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
        require(
            getIndexFile().getMetadata().getCurrentId(),
            equalTo(id)
        );
        require(
            getIndexFile().getMetadata().getCurrentSpec().getType(),
            equalTo(id.getType())
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
        var out = new ArrayList<>(toStream(
                repository.log()
                    .addRange(fromExc, toInc)
                    .call()
            )
            .toList());
        reverse(out);
        return out;
    }

    public RevCommit currentCommit(){
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
        require(
            current.getParentCount() == 1,
            "current commit should have single parent (parent count: %s)",
            current.getParentCount()
        );
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

    //todo remove first arg
    public ObjectId commit(String branchName, String msg){
        return commit(branchName, msg, false);
    }

    //todo ditto
    @SneakyThrows
    public ObjectId commit(String branchName, String msg, boolean amend){
//        log.info("On branch "+ branchesWithHead(currentCommitId()));
//        log.info("Checking out "+branchName);
//        checkoutExisting(branchName);
        repository.add().addFilepattern(".").call();
        repository.add().setUpdate(true).addFilepattern(".").call();
//        log.info("Add ./* called");
        var c = repository.commit().setMessage(msg).setAmend(amend).call();
//        log.info("Commit: "+c);
//        log.info("Merging commit "+c+" to branch "+branchName);
//        repository.merge().include(repository.getRepository().parseCommit(c.getId())).setFastForward(MergeCommand.FastForwardMode.FF_ONLY).call();
        return c.toObjectId();
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
        require(
             count == 1,
            "current commit should be head of current progress branch (called '%s')",
            name
        );
    }

    @SneakyThrows
    public void push(List<String> branches, boolean tags){
        log.info("Push "+branches+" and "+(tags ? "" : "no ")+"tags");
        //fixme https://stackoverflow.com/questions/27823940/jgit-pushing-a-branch-and-add-upstream-u-option
        // what the actual crap, JGIT is broken?
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
