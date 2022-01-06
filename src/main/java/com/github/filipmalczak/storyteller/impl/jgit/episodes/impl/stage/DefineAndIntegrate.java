package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.Episode;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.TaleContext;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.RefSpec;

import java.util.function.Function;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.valid4j.Assertive.require;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Flogger
/**
 * Defines a subepisode (in index of scope and as tag), creates and switches to a progress branch for that episode,
 * tells that subepisode; if it was uniterrupted, merges subepisode progress head to scopes progress and tags it as integrate.
 */
public class DefineAndIntegrate implements Stage {
    @NonNull EpisodeId scope;
    /**
     * The target episode (described by toDefine) is Xth leaf of scope episode. This var holds X (counting from 0).
     */
    int episodeInScopeIdx;
    @NonNull EpisodeSpec toDefine;
    @NonNull Function<EpisodeId, Episode> toRunFactory;
    @NonNull TaleContext context;
    WorkingCopy workingCopy;

    @Override
    @SneakyThrows
    public void body() {
        log.atInfo().log("Planning the stage");
        var shouldCommitDefine = true;
        var shouldTagDefine = true;
        var shouldBranch = true;
        var shouldIntegrate = true;
        var branch = buildRefName(scope, PROGRESS);
        log.atInfo().log("Switching to branch %s");
        workingCopy.checkoutExisting(branch);
        //todo
        var result = workingCopy.getRepository().pull().setRemoteBranchName(branch).call();
        require(result.isSuccessful(), "pulling must be succesful");
        var scopeIndex =  workingCopy.getIndexFile().getMetadata().getOrderedIndex();
        EpisodeId id;
        EpisodeDefinition definition;
        String defineName;
        String integrateName;
        //todo raw copy from DefineAndRun
        if (scopeIndex.size() > episodeInScopeIdx) { // definition for this episode already present
            EpisodeDefinition existing = scopeIndex.get(episodeInScopeIdx);
            id = existing.getEpisodeId();
            definition = new EpisodeDefinition(id, toDefine);
            defineName = buildRefName(id, DEFINE);
            integrateName = buildRefName(id, INTEGRATE, scope);
            log.atFine().log("Episode %s already defined ", definition);
            require(
                toDefine,
                equalTo(existing.getEpisodeSpec())
//                "If the episode has already been defined, then definitions must match"
            ); //todo salvaging happens here!
            shouldCommitDefine = false;
        } else {
            log.atInfo().log("Episode %s not defined yet", toDefine);
            id = EpisodeId.randomId(toDefine.getType(), toDefine.getName());
            log.atInfo().log("Generated ID %s", id);
            definition = new EpisodeDefinition(id, toDefine);
            defineName = buildRefName(id, DEFINE);
            integrateName = buildRefName(id, INTEGRATE, scope);
        }
        if (workingCopy.tagExists(defineName)){
            log.atInfo().log("Tag %s already present", defineName);
            shouldTagDefine = false;
            require(
                !(shouldCommitDefine && !shouldTagDefine),
                "If the episode hasn't already been defined (%s), then it must not have a corresponding tag (%s)",
                shouldCommitDefine, shouldTagDefine
            );
            //todo safeguard that tag is on progress branch
        } else {
            log.atInfo().log("Tag %s missing", defineName);
        }
        var childBranch = buildRefName(id, PROGRESS);
        if (workingCopy.branchExists(childBranch)){
            shouldBranch = false;
            log.atInfo().log("Branch %s already present", childBranch);
            //todo safeguard that it was forked from current progress
        } else {
            log.atInfo().log("Branch %s missing", childBranch);
        }
        if (workingCopy.tagExists(integrateName)){
            log.atInfo().log("Tag %s already present", integrateName);
            //todo check that tag name matches commit name
            shouldIntegrate = false;
            require( //todo tweak messages
                !(shouldTagDefine && !shouldIntegrate),
                "Episode that was just defined must be marked for running"
            );
        }
        log.atInfo().log("Executing the stage");

        if (shouldCommitDefine){
            log.atInfo().log("Defining episode %s in index of episode %s by commiting to its progress", definition, scope);
            doCommitDefine(id, branch, defineName);
            log.atInfo().log("Defining by commiting finished");
        } else {
            log.atInfo().log("Episode %s already defined in index of episode %s", definition, scope);
        }
        if (shouldTagDefine){
            log.atInfo().log("Creating tag %s", defineName);
            doTagDefine(branch, defineName);
            log.atInfo().log("Creating the tag finished");
        } else {
            log.atInfo().log("Tag %s already present", defineName);
        }
        if (shouldBranch){
            log.atInfo().log("Creating branch %s", childBranch);
            doBranch(childBranch);
            log.atInfo().log("Creating the branch finished");
        } else {
            log.atInfo().log("Branch %s already present", childBranch);
        }
        log.atInfo().log("Executing child episode %s", toDefine);
        var episode = toRunFactory.apply(id);
        log.atFine().log("Child episode impl: %s", episode);
        episode.tell(context);
        if (shouldIntegrate){
            log.atInfo().log("Integrating subepisode %s to branch %s", toDefine, childBranch);
            doCommitAndTagIntegrate(branch, childBranch, buildRefName(id, END), integrateName);
            log.atInfo().log("Integration finished");
        } else {
            log.atInfo().log("Integration of subepisode %s to branch %s has already happened", toDefine, childBranch);
        }
    }

    private void doCommitDefine(EpisodeId id, String scopeBranch, String defineName){
        //fixme copypaste
        getWorkingCopy()
            .getIndexFile()
            .updateMetadata(
                m ->
                    m.toBuilder()
                        .subEpisode(new EpisodeDefinition(id, toDefine))
                        .build()
            );
        log.atFine().log("Metadata of %s extended with descriptor of %s; persisting", scope, toDefine);
        getWorkingCopy().commit(scopeBranch, defineName);
        getWorkingCopy().push(asList(scopeBranch), false);
        log.atFine().log("Commited and pushed");
    }

    private void doTagDefine(String scopeBranch, String defineName){
        require( //todo use this in other places that create tags
            workingCopy.currentCommit().getFullMessage(),
            equalTo(defineName)
//            "Name of the tagged commit must match the commit message"
        );
        workingCopy.createTag(defineName);
        log.atFine().log("Tag %s created", defineName);
        workingCopy.push(asList(scopeBranch), true);
        log.atFine().log("Changes pushed");
        workingCopy.checkoutExisting(scopeBranch);
        log.atFine().log("Switched back to scope progress branch");
    }

    @SneakyThrows //todo
    private void doBranch(String childBranch){
        //todo invariant: on scope progress
        //todo is this really that simple? I feel that Im missing smth
        workingCopy.createBranch(childBranch);
        workingCopy.getRepository().push() //todo
            .setRemote("origin")
            .setRefSpecs(asList(new RefSpec(childBranch+":"+childBranch)))
            .call();
        workingCopy.push(asList(childBranch), false);
        log.atFine().log("Changes pushed");
        log.atFine().log("Staying at the subtasks progress branch");
    }

    //todo start commit/tag are splitted; should we split this too or merge the other ones?
    //I think that this should be together for transactionality (either commit+tag are pushed or neither)
    //while start can be split (we allow failure between commit and tag)
    //if we allowed failure during integration, then head of scope progress would move, so some previous invariants may not hold anymore
    @SneakyThrows
    private void doCommitAndTagIntegrate(String scopeBranch, String childProgressBranch, String childEndTag, String integrateName){
//    private void doCommitAndTagIntegrate(String scopeBranch, String childProgressBranch, String childEndTag, String integrateName){
//        invariant(
//            workingCopy.resolveToCommitId(childProgressBranch).equals(workingCopy.resolveToCommitId(childEndTag)),
//            "Progress of the integration subject must be the same as its end marker"
//        );
        //todo invariant above is valid for subsequences, not for leaves though; tweak and reenable
        log.atFine().log("Switching to scope progress branch %s", scopeBranch);
//        workingCopy.getRepository().reset().addPath(".episode-index").setRef(scopeBranch).call();//todo
        var pullScope = workingCopy.getRepository().pull().setRemoteBranchName(scopeBranch).call();
        var pullChild = workingCopy.getRepository().pull().setRemoteBranchName(childProgressBranch).call();
        log.atFine().log("Pulling status: scope=%s, child=%s", pullScope.isSuccessful(), pullChild.isSuccessful());
        workingCopy.checkoutExisting(scopeBranch);
        var meta = workingCopy.getIndexFile().getMetadata();
        log.atFine().log("Retrieved metadata: %s", meta);
        log.atFine().log("Switched; merging child %s to scope %s", childProgressBranch, scopeBranch);
        var result = workingCopy.getRepository().merge() //todo encapsulate in working copy?
            .setFastForward(MergeCommand.FastForwardMode.NO_FF)
//            .include()
//            .include(workingCopy.getBranch(childProgressBranch).get())
//            .include(workingCopy.getRepository().getRepository().resolve(childEndTag))
//            .include(workingCopy.getRepository().getRepository().resolve(scopeBranch))
            .include(workingCopy.getRepository().getRepository().resolve(childProgressBranch))
            .setContentMergeStrategy(ContentMergeStrategy.CONFLICT)
            .setStrategy(MergeStrategy.SIMPLE_TWO_WAY_IN_CORE)
            .setMessage(integrateName)
//            .setCommit(true)
            .call();
        log.atInfo().log("Merge status: %s", result.getMergeStatus());
        workingCopy.getIndexFile().setMetadata(meta);
        log.atFine().log("Metadata set to %s", meta);
        workingCopy.commit(scopeBranch, integrateName);
        log.atFine().log("Merged; creating tag %s", integrateName);
        workingCopy.createTag(integrateName);
        log.atFine().log("Created; pushing both branches + tags");
        workingCopy.push(asList(scopeBranch, childProgressBranch), true);

    }


}
