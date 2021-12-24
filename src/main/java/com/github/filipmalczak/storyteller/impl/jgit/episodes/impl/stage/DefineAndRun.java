package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage;

import com.github.filipmalczak.storyteller.api.storage.Storage;
import com.github.filipmalczak.storyteller.api.story.ActionBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.github.filipmalczak.storyteller.impl.jgit.storage.data.DirectoryStorage;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.Date;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;
import static java.util.Arrays.asList;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Flogger
/**
 * Defines a leaf episode (in index of scope). If proof commit for that leaf is missing, runs it, commits its meta
 * together with its storage state
 */
public class DefineAndRun implements Stage {
    @NonNull EpisodeId scope;
    /**
     * The target episode (described by toDefine) is Xth leaf of scope episode. This var holds X (counting from 0).
     */
    @NonNull int episodeInScopeIdx;
    @NonNull EpisodeSpec toDefine;
    @NonNull ActionBody<Storage> toRun;
    WorkingCopy workingCopy;

    @Override
    public void body() {
        log.atInfo().log("Planning the stage");
        //set up values
        var scopeStart = buildRefName(scope, START);
        var branch = buildRefName(scope, PROGRESS);
        //navigate to scopes progress
        log.atInfo().log("Switching to branch %s");
        workingCopy.checkoutExisting(branch);

        boolean shouldDefine = true;
        boolean shouldRun = true;
        EpisodeId id;
        EpisodeDefinition definition;
        String defineName;
        String runName;
        var scopeIndex =  workingCopy.getIndexFile().getMetadata().getOrderedIndex();
        if (scopeIndex.size() > episodeInScopeIdx){ // definition for this episode already present
            EpisodeDefinition existing = scopeIndex.get(episodeInScopeIdx);
            id = existing.getEpisodeId();
            definition = new EpisodeDefinition(id, toDefine);
            defineName = buildRefName(id, DEFINE);
            runName = buildRefName(id, RUN);
            log.atFine().log("Episode %s already defined ", definition);
            invariant(
                toDefine.equals(existing.getEpisodeSpec()),
                "If the leaf has already been defined, then definitions must match"
            ); //todo salvaging happens here!
            shouldDefine = false;
        } else { //not defined yet
            log.atInfo().log("Episode %s not defined yet", toDefine);
            id = EpisodeId.randomId(toDefine.getType(), toDefine.getName());
            log.atInfo().log("Generated ID %s", id);
            definition = new EpisodeDefinition(id, toDefine);
            defineName = buildRefName(id, DEFINE);
            runName = buildRefName(id, RUN);
        }
        var commits = workingCopy.gitLog(workingCopy.resolveToCommitId(scopeStart), workingCopy.resolveToCommitId(branch));
        log.atFine().log("Commits log: %s", commits);
        log.atFine().log("Commits log length: %s; idx: %s", commits.size(), episodeInScopeIdx);
        //size > 2*idx
        invariant(
            shouldDefine || commits.size() > 2*episodeInScopeIdx,
            "Either the leaf hasn't yet been defined, or the commit log has a required minimum length"
        );
        //log[2*idx] == define
        invariant(
            shouldDefine || commits.get(2*episodeInScopeIdx).getFullMessage().equals(defineName),
            "First commit for an already defined leaf should be a define commit"
        );

        if (commits.size() > 2*episodeInScopeIdx+1){
            var commit = commits.get(2*episodeInScopeIdx+1);
            invariant(
                commit.getFullMessage().equals(runName),
                "If second commit for an already defined leaf is present, then it must be a run commit"
            );
            // see https://www.programcreek.com/java-api-examples/?class=org.eclipse.jgit.revwalk.RevCommit&method=getCommitTime
            // to figure out why *1000
            DefineAndRun.log.atInfo().log("Episode %s already ran on %s", id, new Date(commit.getCommitTime()*1000L));
            shouldRun = false;
        }
        invariant(
            !(shouldDefine && !shouldRun),
            "Episode that was just defined must be marked for running"
        );
        log.atInfo().log("Executing the stage");
        if (shouldDefine){
            log.atInfo().log("Defining episode %s in index of episode %s", definition, scope);
            doDefine(id, branch, defineName);
            log.atInfo().log("Defining finished");
        } else {
            log.atInfo().log("Episode %s already defined in index of episode %s", definition, scope);
        }
        if (shouldRun){
            log.atInfo().log("Running body of episode %s", definition);
            doRun(id, branch, runName);
            log.atInfo().log("Run finished");
        } else {
            log.atInfo().log("Episode %s already executed", definition);
        }
    }

    private void doDefine(EpisodeId id, String branch, String define){
        //todo invariant: previous commit is either scope start or a run commit of any other leaf
        getWorkingCopy()
            .getIndexFile()
            .updateMetadata(
                m ->
                    m.toBuilder()
                        .subEpisode(new EpisodeDefinition(id, toDefine))
                        .build()
            );
        log.atFine().log("Metadata of %s extended with descriptor of %s; persisting", scope, toDefine);
        getWorkingCopy().commit(branch, define);
        getWorkingCopy().push(asList(branch), false);
        log.atFine().log("Commited and pushed");
    }

    private void doRun(EpisodeId id, String branch, String run){
        //todo invariant: prev commit is define of this id
        var prevMeta = getWorkingCopy().getIndexFile().getMetadata();
        log.atFine().log("Metadata of scope: %s", prevMeta);
        try {
            getWorkingCopy()
                .getIndexFile()
                .setMetadata(Metadata.buildMetadata(new EpisodeDefinition(id, toDefine), scope));
            log.atFine().log("Metadata updated to descriptor of %s; executing", toDefine);
            var storage = new DirectoryStorage(workingCopy.getRepository().getRepository().getDirectory()); //todo helper in workingcopy
            toRun.action(storage);
            log.atFine().log("Executed; persisting");
            getWorkingCopy().commit(branch, run);
            getWorkingCopy().push(asList(branch), false);
            log.atFine().log("Commited and pushed");
        } finally {
            getWorkingCopy().getIndexFile().setMetadata(prevMeta);
            log.atFine().log("Metadata reverted (without commiting) to metadata of scope");
        }
    }
}
