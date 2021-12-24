package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.util;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.Episode;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.tree.SubEpisode;
import com.github.filipmalczak.storyteller.impl.jgit.storage.DiskSpaceManager;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.github.filipmalczak.storyteller.impl.jgit.storage.Workspace;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.MergeCommand;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.filipmalczak.storyteller.impl.jgit.episodes.Episode.getEpisodeDefinition;
import static com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata.buildMetadata;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@Slf4j
/**
 * This class doesn't care if described episode can use given simple command; e.g. run() should only be available
 * for leaves, and yet this class may be used for arc or thread.
 */
public class Commands {
    EpisodeId parentId; //nullable
    @NonNull EpisodeDefinition definition;
    @NonNull Workspace workspace;
    @NonNull DiskSpaceManager manager;
//    @NonNull EpisodeSimpleCommands.ProgressLoader<ProofType> loader;

    public EpisodeType getEpisodeType(){
        return definition.getEpisodeId().getType();
    }

    public EpisodeId getEpisodeId(){
        return definition.getEpisodeId();
    }

    public EpisodeSpec getEpisodeSpec(){
        return definition.getEpisodeSpec();
    }

    public String getEpisodeName(){
        return definition.getEpisodeSpec().getName();
    }

//    public Commands(EpisodeId parentId, @NonNull EpisodeDefinition definition, @NonNull Workspace workspace, @NonNull DiskSpaceManager manager, @NonNull Commands.ProgressLoader<ProofType> loader) {
//        this.parentId = parentId;
//        this.definition = definition;
//        this.workspace = workspace;
//        this.manager = manager;
//        this.loader = loader;
//    }

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Getter(lazy = true)
    WorkingCopy workingCopy = manager.open(workspace);

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Getter(lazy = true)
    String parentProgressBranchName = buildRefName(parentId, PROGRESS);

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Getter(lazy = true)
    String progressBranchName = buildRefName(getEpisodeId(), PROGRESS);


    private boolean parentProgressExists(){
        return getWorkingCopy().branchExists(getParentProgressBranchName()); //todo and its metadata is well-initialized?
    }

    private boolean currentProgressExists(){
        return getWorkingCopy().branchExists(getProgressBranchName()); //todo and its metadata is well-initialized?
    }

    /**
     * Performs no "already defined" checks! It just add an adequate entry to THIS EPISODES index.
     * Enters anywhere and exits on current progress branch.
     */
    public void define(EpisodeDefinition definition){
        invariant(
            currentProgressExists(),
            "current episodes progress branch should already be present in the working copy!"
        );
        var branchName = getProgressBranchName();
        getWorkingCopy().checkoutExisting(branchName);
        getWorkingCopy()
            .getIndexFile()
            .updateMetadata(
                m ->
                    m.toBuilder()
                        .subEpisode(definition)
                        .build()
            );
        log.info("Commiting: Define episode "+definition+" in index on branch "+branchName);
        getWorkingCopy().commit(branchName, buildRefName(definition.getEpisodeId(), DEFINE, getEpisodeId()));
        pushCurrentProgress(false);
    }

    /**
     * Applicable only to sequences.
     * If missing, create and push progress branch; read all known episode definitions in order and
     * gather their run proof.
     * Enters on parent-progress-branch-ish, ideally on define (but not required; if parentIdf==null, you
     * may manually check out branching point); exits on progress branch.
     * @return list of already defined child episodes
     */
    public List<EpisodeDefinition> initializeSequenceProgress(){
        log.info("init "+getEpisodeId());
        if (getWorkingCopy().branchExists(getProgressBranchName())){
            log.info("branch "+getProgressBranchName()+" exists");
            getWorkingCopy().checkoutExisting(getProgressBranchName());
            return getWorkingCopy().getIndexFile().getMetadata().getOrderedIndex();
            //todo safeguard starts with define present on parent then start that is commited from define
        } else {
            getWorkingCopy().createBranch(getProgressBranchName());
            return emptyList();
        }
    }

    //todo start/end should take somethind like Expectation=enum(DONE, PENDING) and fail if it doesnt match tag.exists
    private void sequenceLifecycleTag(String tagName, Consumer<WorkingCopy> existingValidators, Metadata metadata){
        getWorkingCopy().checkoutExisting(getProgressBranchName());
        if (getWorkingCopy().tagExists(tagName)){
            log.info("Tag "+tagName+" already exists!");
            getWorkingCopy().checkoutExisting(tagName);
            existingValidators.accept(getWorkingCopy());

        } else {
            log.info("Creating tag "+tagName);
            getWorkingCopy()
                .getIndexFile()
                .setMetadata(metadata);
            getWorkingCopy().commit(getProgressBranchName(), tagName);
            getWorkingCopy().createTag(tagName);;
        }
        getWorkingCopy().checkoutExisting(getProgressBranchName());
        pushCurrentProgress(true);
    }

    /**
     * If start tag is missing, sanitizes the metadata and tags it;
     * if it isn't, its validated (parentCommit=define, partOfBranch=progress, prev commit on progress is missing)
     * Enters and exits on progress branch.
     */
    public void startSequence(){
        sequenceLifecycleTag(
            buildRefName(getEpisodeId(), START),
            (wc) -> {
                wc.safeguardValidIndexFile(getEpisodeId());//todo check name as well
                wc.safeguardSingleParentWithTag("whatever"); //todo tagName is ignored; see assertLastTagIsEndTag below
            },
            buildMetadata(definition, parentId)
        );

    }

    /**
     * If end tag is missing, sanitizes the metadata and tags it;
     * if it isn't, its validated (partOfBranch=progress, next commit on progress is missing)
     * Enters and exits on progress branch.
     *
     */
    public void endSequence(){
        getWorkingCopy().checkoutExisting(getParentProgressBranchName());
        var startMeta = getWorkingCopy().getIndexFile().getMetadata();
        getWorkingCopy().checkoutExisting(getProgressBranchName());
        sequenceLifecycleTag(
            buildRefName(getEpisodeId(), END),
            (wc) -> {
                //todo validate existing end tag
            },
            startMeta
        );
    }

    /**
     * Integrates this episode (a branch) into parent episode (also a branch).
     * If integration tag is mssing, integrates (merges, sanitizes metadata, tags);
     * if not, validates (partOfBranch=parent.progress, parentCommit=end).
     * Requires parentId != null.
     * Enters on progress branch, exits on parent progress branch.
     */
    @SneakyThrows
    public void integrate(){
        invariant(
            parentProgressExists(),
            "parent progress branch should already be present in the working copy!"
        );
        getWorkingCopy().checkoutExisting(getProgressBranchName());
        //todo this should be possible without checkout, by raw plumbing api
        var metadata = getWorkingCopy().getIndexFile().getMetadata(); //current meta
        log.info("checkout ok");
        var tagName = buildRefName(getEpisodeId(), INTEGRATE, parentId);
        log.info("tag: "+tagName);
        if (getWorkingCopy().tagExists(tagName)) {
            log.info("integration tag already exists!");
//                workingCopy.checkoutExisting("/refs/heads/"+tagName);
            //todo perform validation - critical
            //  assert proper parents (one tag with x-end last on x-progress, the other y-start, previous on y-progress)
            //  assert proper message ??
        } else {
            log.info("integration tag is missing, we're gonna merge!");
            getWorkingCopy().checkoutExisting(getParentProgressBranchName());
            getWorkingCopy().getRepository()
                .merge()
                .include(getWorkingCopy().getBranch(getProgressBranchName()).get())
                .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                //todo message?
                .call();
            getWorkingCopy().getIndexFile().setMetadata(metadata);
            //todo this is pretty repeatable -> extract method here
            getWorkingCopy().commit(getParentProgressBranchName(), tagName);
            log.info("gonna tag");
            getWorkingCopy().createTag(tagName);
            log.info("pushing " + parentProgressBranchName + " and tags");
            getWorkingCopy().push(asList(getParentProgressBranchName(), getProgressBranchName()), true);
            log.info("here ya go");
        }
    }

    public void pushCurrentProgress(boolean tags){
        getWorkingCopy().push(asList(getProgressBranchName()), tags);
    }

    public void pushParentProgress(){
        pushParentProgress(false);
    }

    public void pushParentProgress(boolean tags){
        getWorkingCopy().push(asList(getParentProgressBranchName()), tags);
    }

    public static Commands getCommands(Episode episode, Workspace workspace, DiskSpaceManager manager){
        return new Commands(
            null,
            getEpisodeDefinition(episode),
            workspace,
            manager
        );
    }

    //todo not the best place for this
    public static <T> Optional<T> pop(List<T> l){
        return l.stream().peek(x -> l.remove(0)).findFirst();
    }

    //todo ditto
    public static EpisodeDefinition handleDefinition(Optional<EpisodeDefinition> expected, EpisodeSpec spec, Consumer<EpisodeDefinition> persistDefinition){
        log.info("Figuring out a definition: "+expected+" "+spec);
        if (expected.isEmpty()){
            var id = EpisodeId.randomId(spec.getType(), spec.getName());
            log.info("Using id: "+id);
            var def = new EpisodeDefinition(id, spec);
            log.info("Defining "+def);
            persistDefinition.accept(def);
            log.info("Returning "+def);
            return def;
        }
        var exp = expected.get();
        log.info("Expected def: "+exp);
        if (spec.equals(exp.getEpisodeSpec())) {
            log.info("Definition matches");
            return exp;
        }
        log.info("Definition doesnt match the expected one");
        throw new RuntimeException(); //todo this is the place where you can salvage some work
    }

    public static Commands getCommands(SubEpisode episode, Workspace workspace, DiskSpaceManager manager){
        return new Commands(
            episode.getParentId(),
            getEpisodeDefinition(episode),
            workspace,
            manager
        );
    }

    public void safeguardOnProgressHead(){
        getWorkingCopy().safeguardOnBranchHead(buildRefName(getEpisodeId(), PROGRESS));

    }
}
