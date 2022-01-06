package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.DefinitionFactory;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StageBody;
import com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils.StartPointFactory;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.github.filipmalczak.storyteller.impl.jgit.storage.index.Metadata;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.eclipse.jgit.lib.ObjectId;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.valid4j.Assertive.require;

@Flogger
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExecuteLeaf implements Stage{
    @NonNull EpisodeId parentId;
    @NonNull EpisodeId leafId;
    final StartPointFactory startPointFactory = StartPointFactory.buildRef(DEFINE);
    final DefinitionFactory definitionFactory = DefinitionFactory.RETRIEVE_FROM_PARENT_INDEX;
    @NonNull StageBody body;
//    @Builder.Default
//    boolean containsLeaves = false; //if false, then this is a node or root sequence; if true - leaf sequence

    @Setter WorkingCopy workingCopy;

    @Override
    public EpisodeId getScope() {
        return leafId;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @ToString
    private class Progress {
        boolean shouldCommitStart;
        boolean shouldTagStart;
        boolean shouldRun;
//        boolean shouldCommitReconciliation;
//        boolean shouldTagEnd;

        String progressName;

        ObjectId initialLatestCommit;

        String startName;
        String runName;
//        String endName;
//        String reconciliationName;

        String branchOffRef;
        ObjectId branchOffCommit;


        @SneakyThrows
        public void init(){
            shouldCommitStart = true;
            shouldTagStart = true;
            shouldRun = true;
//            shouldCommitReconciliation = containsLeaves;
//            shouldTagEnd = true;

            progressName = buildRefName(leafId, PROGRESS);
            //todo
            var result = workingCopy.getRepository().pull().setRemoteBranchName(progressName).call();
            require(result.isSuccessful(), "pulling must be succesful");

            initialLatestCommit = workingCopy.resolveToCommitId(progressName);

            startName = buildRefName(leafId, START);
            runName = buildRefName(leafId, RUN);
//            endName = buildRefName(leafId, END);
//            reconciliationName = buildRefName(sequenceId, RECONCILE);

            branchOffRef = startPointFactory.apply(leafId);
            branchOffCommit = workingCopy.resolveToCommitId(branchOffRef);
            log.atFine().log("Initialized progress to %s", this);
        }

        public void plan(){
            log.atInfo().log("Planning the stage");
            log.atInfo().log("Switching to branch %s");
            workingCopy.checkoutExisting(progressName);
            log.atFine().log(
                "Looking up commits between branch off point (%s -> <%s>) and progress head (%s -> <%s>)",
                branchOffRef, branchOffCommit,
                progressName, initialLatestCommit
            );
            var sequenceCommits = workingCopy.gitLog(branchOffCommit, initialLatestCommit);
            if (sequenceCommits.size() > 0){
                log.atInfo().log("Initial branch commit for sequence %s present", leafId);
                shouldCommitStart = false;
                //todo checkout commits[0], safeguard that metadata from index file matches id, checkout back
            } else {
                log.atInfo().log("Initial branch commit for sequence %s missing", leafId);
            }
            if (workingCopy.tagExists(startName)){
                log.atInfo().log("Tag %s already present", startName);
                shouldTagStart = false;
            } else {
                log.atInfo().log("Tag %s missing", startName);
            }
            require(
                !(shouldCommitStart && !shouldTagStart),
                "If initial commit isn't present yet (%s), then start tag cannot be present neither (%s)",
                shouldCommitStart, shouldTagStart
            );
            if (sequenceCommits.size() > 1){
                log.atInfo().log("Run commit for leaf %s present", leafId);
                shouldRun = false;
                //todo invariant commit name matches
            } else {
                log.atInfo().log("Run commit for leaf %s is missing", leafId);
            }
//            if (containsLeaves) {
//                log.atInfo().log("Sequence contains leaves, may need to commit index reconciliation");
//                if (sequenceCommits.isEmpty() || !sequenceCommits.get(sequenceCommits.size() - 1).getFullMessage().equals(reconciliationName)){
//                    log.atInfo().log("Reconciliation commit %s missing", reconciliationName);
//                    shouldCommitReconciliation = true;
//                } else {
//                    log.atInfo().log("Reconciliation commit %s present at the end of curren");
//                }
//            }
//            if (workingCopy.tagExists(endName)){
//                log.atInfo().log("Tag %s already present", endName);
//                shouldTagEnd = false;
//                //todo check what is tagged - either an integrate of last child, or reconcile commit of leaf sequence
//            } else {
//                log.atInfo().log("Tag %s missing", endName);
//            }
//            invariant(
//                !(shouldTagStart && !shouldTagEnd),
//                "If start tag isn't present yet, then end tag cannot be present neither"
//            );
//            invariant(
//                !(shouldCommitReconciliation && !shouldTagEnd),
//                "If reconciliation commit isn't present yet, then end that cannot be present neither"
//            );
        }

        public void exec(){
            log.atInfo().log("Executing the stage");

            if (shouldCommitStart){
                log.atInfo().log("Commiting adequate metadata of episode %s to its branch", leafId);
                doCommitStart();
                log.atInfo().log("Commiting metadata finished");
            } else {
                log.atInfo().log("Metadata of episode %s has already been commited at the beginning of the branch", leafId);
            }
            if (shouldTagStart){
                log.atInfo().log("Creating tag %s", startName);
                doTagStart();
                log.atInfo().log("Creating the tag finished");
            } else {
                log.atInfo().log("Tag %s already present", startName);
            }
            if (shouldRun) {
                log.atInfo().log("Running leaf %s body", leafId);
                body.run();
                log.atInfo().log("Running body of %s finished; commiting results", leafId);
                workingCopy.commit(progressName, runName);
                log.atInfo().log("Commited; pushing");
                workingCopy.push(asList(progressName), false);
                log.atInfo().log("Pushed");
            } else {
                log.atInfo().log("Skipping run of %s", leafId);
                //todo details of why were skipping, e.g, when run happened originally
            }
//            if (shouldCommitReconciliation){
//                log.atInfo().log("Reconciling index of current branch");
//                doReconcile();
//                log.atInfo().log("Reconciliation done");
//            }

        }


        //fixme awfuly similar to doCommitDefine and doTagDefine from the other stages
        private void doCommitStart(){
            require(
                workingCopy.resolveToCommitId(branchOffRef),
                equalTo(workingCopy.resolveToCommitId(progressName))
//                "Start commit must be performed from sequence branch that has been tagged on its head as sequence definition"
            );
            var sequenceDef = definitionFactory.apply(workingCopy);
            workingCopy.getIndexFile().setMetadata(Metadata.buildMetadata(sequenceDef, parentId));
            log.atFine().log("Metadata set to description of %s; persisting", sequenceDef);
            getWorkingCopy().commit(progressName, startName);
            getWorkingCopy().push(asList(progressName), false);
            log.atFine().log("Commited and pushed");
        }

        //fixme ditto
        private void doTagStart(){
            require( //todo use this in other places that create tags
                workingCopy.currentCommit().getFullMessage(),
                equalTo(startName)
//                "Name of the tagged commit must match the commit message"
            );
            //todo invariant: on progress head; does this apply in other places too?
            workingCopy.createTag(startName);
            log.atFine().log("Tag %s created", startName);
            workingCopy.push(asList(progressName), true);
            log.atFine().log("Changes pushed");
            workingCopy.checkoutExisting(progressName);
            log.atFine().log("Switched back to sequence progress branch");
        }
//
//        //fixme ditto
//        private void doTagEnd(){
//            //todo invariant: on progress head; does this apply in other places too?
//            workingCopy.createTag(endName);
//            log.atFine().log("Tag %s created", endName);
//            workingCopy.push(asList(progressName), true);
//            log.atFine().log("Changes pushed");
//            workingCopy.checkoutExisting(progressName);
//            log.atFine().log("Switched back to sequence progress branch");
//        }

//        private void doReconcile(){
//            //todo invariant: on progress head; does this apply in other places too?
//            //todo invariant: on last leaf run commit
//            invariant(
//                workingCopy.currentCommit().getParentCount() == 1,
//                "todo"//todo
//            );
//            var parent = workingCopy.currentCommit().getParent(0);
//            var sParent = parent.toObjectId().getName();
//            log.atFine().log("Parent is: %s", sParent);
//            workingCopy.checkoutExisting(sParent);
//            log.atFine().log("Switched to parent commit");
//            var meta = workingCopy.getIndexFile().getMetadata();
//            log.atFine().log("Retrieved metadata: %s", meta);
//            workingCopy.checkoutExisting(progressName);
//            log.atFine().log("Switched back to sequence progress branch");
//            workingCopy.getIndexFile().setMetadata(meta);
//            log.atFine().log("Metadata set to the one retrieved from previous commit");
//            getWorkingCopy().commit(progressName, reconciliationName);
//            getWorkingCopy().push(asList(progressName), false);
//            log.atFine().log("Commited and pushed");
//        }
    }

    @Override
    public void body() {
        var progress = new Progress();
        progress.init();
        progress.plan();
        progress.exec();
    }



}

