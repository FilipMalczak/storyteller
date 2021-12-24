package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeDefinition;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeSpec;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeType;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.PersistenceHack.pushItTheFuckOuttaHere;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Flogger
/**
 * Assumes that progress branch already exists.
 * fixme its almost the same as exec sequence; there is one invariant missing, some attrs are added here, the way the
 * definition is derived is different and commit list is started from empty instead of define
 */
public class ExecuteRoot implements Stage {
    @NonNull EpisodeId sequenceId;
    @NonNull EpisodeType type;
    @NonNull String name;
    WorkingCopy workingCopy;
    @NonNull Runnable exec;

    @Override
    public EpisodeId getScope() {
        return sequenceId;
    }

    @Override
    public void body() {
        log.atInfo().log("Planning the stage");
        var shouldCommitStart = true;
        var shouldTagStart = true;
        var shouldTagEnd = true;
        var sequenceBranch = buildRefName(sequenceId, PROGRESS);
        var startTag = buildRefName(sequenceId, START);
        var endTag = buildRefName(sequenceId, END);
        log.atInfo().log("Switching to branch %s", sequenceBranch);
        workingCopy.checkoutExisting(sequenceBranch);
        //"define" as in "define sequence"
        var progressHead = workingCopy.resolveToCommitId(sequenceBranch);
        //todo where does story branch off of?
        var empty = workingCopy.resolveToCommitId("empty");
        log.atFine().log("Looking up commits between empty (%s) and progress head (%s)", empty, progressHead);
        var sequenceCommits = workingCopy.gitLog(empty, progressHead);
        if (sequenceCommits.size() > 0){
            log.atInfo().log("Initial branch commit for sequence %s present", sequenceId);
            shouldCommitStart = false;
            //todo checkout commits[0], safeguard that metadata from index file matches id, checkout back
        } else {
            log.atInfo().log("Initial branch commit for sequence %s missing", sequenceId);
        }
        if (workingCopy.tagExists(startTag)){
            log.atInfo().log("Tag %s already present", startTag);
            shouldTagStart = false;
        } else {
            log.atInfo().log("Tag %s missing", startTag);
        }
        invariant(
            !(shouldCommitStart && !shouldTagStart),
            "If initial commit isn't present yet, then start tag cannot be present neither"
        );
        if (workingCopy.tagExists(endTag)){
            log.atInfo().log("Tag %s already present", endTag);
            shouldTagEnd = false;
        } else {
            log.atInfo().log("Tag %s missing", endTag);
        }
        invariant(
            !(shouldTagStart && !shouldTagEnd),
            "If start tag isn't present yet, then end tag cannot be present neither"
        );
        log.atInfo().log("Executing the stage");

        if (shouldCommitStart){
            log.atInfo().log("Commiting adequate metadata of episode %s to its branch", sequenceId);
            doCommitStart(sequenceBranch, startTag);
            log.atInfo().log("Commiting metadata finished");
        } else {
            log.atInfo().log("Metadata of episode %s has already been commited at the beginning of the branch", sequenceId);
        }
        if (shouldTagStart){
            log.atInfo().log("Creating tag %s", startTag);
            doTagStart(sequenceBranch, startTag);
            log.atInfo().log("Creating the tag finished");
        } else {
            log.atInfo().log("Tag %s already present on %s", startTag, getWorkingCopy().resolveToCommitId(startTag));
        }
        exec.run();
        if (shouldTagEnd){
            log.atInfo().log("Creating tag %s", endTag);
            doTagEnd(sequenceBranch, endTag);
            log.atInfo().log("Creating the tag finished");
        } else {
            log.atInfo().log("Tag %s already present on", endTag, getWorkingCopy().resolveToCommitId(endTag));
        }
    }

    //fixme awfuly similar to doCommitDefine and doTagDefine from the other stages
    private void doCommitStart(String sequenceBranch, String startSequenceName) {
        //todo invariant: parent is empty?
        var indexFile = getWorkingCopy()
            .getIndexFile();
        var sequenceDef = new EpisodeDefinition(sequenceId, new EpisodeSpec(type, name, emptyMap()));
        log.atFine().log("Definition of sequence is %s", sequenceDef);
        indexFile.setMetadata(Metadata.buildMetadata(sequenceDef, null));
        log.atFine().log("Metadata set to description of %s; persisting", sequenceDef);
        var commitId = getWorkingCopy().commit(sequenceBranch, startSequenceName);
//        getWorkingCopy().push(asList(sequenceBranch), false); //fixme
        pushItTheFuckOuttaHere(getWorkingCopy());
        log.atFine().log("Commited %s and pushed", commitId);
    }

    //fixme ditto
    private void doTagStart(String sequenceBranch, String startTag){
        invariant( //todo use this in other places that create tags
            workingCopy.currentCommit().getFullMessage().equals(startTag),
            "Name of the tagged commit must match the commit message"
        );
        //todo invariant: on progress head; does this apply in other places too?
        workingCopy.createTag(startTag);
        log.atFine().log("Tag %s created on %s", startTag, getWorkingCopy().currentCommitId());
        workingCopy.push(asList(sequenceBranch), true);
        log.atFine().log("Changes pushed");
        workingCopy.checkoutExisting(sequenceBranch);
        log.atFine().log("Switched back to sequence progress branch on %s", getWorkingCopy().currentCommitId());
    }

    //fixme ditto
    private void doTagEnd(String sequenceBranch, String endTag){
        //todo invariant: on progress head; does this apply in other places too?
        workingCopy.createTag(endTag);
        log.atFine().log("Tag %s created on %s", endTag, getWorkingCopy().currentCommitId());
        workingCopy.push(asList(sequenceBranch), true);
        log.atFine().log("Changes pushed");
        workingCopy.checkoutExisting(sequenceBranch);
        log.atFine().log("Switched back to sequence progress branch on %s", getWorkingCopy().currentCommitId());
    }
}