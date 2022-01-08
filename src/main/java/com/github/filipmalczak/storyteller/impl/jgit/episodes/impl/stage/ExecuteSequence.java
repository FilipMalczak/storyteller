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
public class ExecuteSequence implements Stage{
    @Builder.Default
    EpisodeId parentId = null;
    @NonNull EpisodeId sequenceId;
    @NonNull StartPointFactory startPointFactory;
    @NonNull DefinitionFactory definitionFactory;
    @NonNull StageBody body;

    @Setter WorkingCopy workingCopy;

    @Override
    public EpisodeId getScope() {
        return sequenceId;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @ToString
    private class Progress {
        boolean shouldCommitStart;
        boolean shouldTagStart;
        boolean shouldTagEnd;

        String progressName;

        ObjectId initialLatestCommit;

        String startName;
        String endName;

        String branchOffRef;
        ObjectId branchOffCommit;

        @SneakyThrows
        public void init(){
            shouldCommitStart = true;
            shouldTagStart = true;
            shouldTagEnd = true;

            progressName = buildRefName(sequenceId, PROGRESS);
            //todo
            var result = workingCopy.getRepository().pull().setRemoteBranchName(progressName).call();
            require(result.isSuccessful(), "pulling must be succesful");

            initialLatestCommit = workingCopy.resolveToCommitId(progressName);

            startName = buildRefName(sequenceId, START);
            endName = buildRefName(sequenceId, END);

            branchOffRef = startPointFactory.apply(sequenceId);
            branchOffCommit = workingCopy.resolveToCommitId(branchOffRef);
            log.atFine().log("Initialized progress to %s", this);
        }

        public void plan(){
            log.atInfo().log("Planning the stage");
            log.atInfo().log("Switching to branch %s", progressName);
            workingCopy.checkoutExisting(progressName);
            log.atFine().log(
                "Looking up commits between branch off point (%s -> <%s>) and progress head (%s -> <%s>)",
                branchOffRef, branchOffCommit,
                progressName, initialLatestCommit
            );
            var sequenceCommits = workingCopy.gitLog(branchOffCommit, initialLatestCommit);
            if (sequenceCommits.size() > 0){
                log.atInfo().log("Initial branch commit for sequence %s present", sequenceId);
                shouldCommitStart = false;
                //todo checkout commits[0], safeguard that metadata from index file matches id, checkout back
            } else {
                log.atInfo().log("Initial branch commit for sequence %s missing", sequenceId);
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
            if (workingCopy.tagExists(endName)){
                log.atInfo().log("Tag %s already present", endName);
                shouldTagEnd = false;
                //todo check what is tagged - either an integrate of last child, or reconcile commit of leaf sequence
            } else {
                log.atInfo().log("Tag %s missing", endName);
            }
            require(
                !(shouldTagStart && !shouldTagEnd),
                "If start tag isn't present yet (%s), then end tag cannot be present neither (%s)",
                shouldTagStart, shouldTagEnd
            );
            log.atInfo().log("Planning resulted in %s", this);
        }

        public void exec(){
            log.atInfo().log("Executing the stage");

            if (shouldCommitStart){
                log.atInfo().log("Commiting adequate metadata of episode %s to its branch", sequenceId);
                doCommitStart();
                log.atInfo().log("Commiting metadata finished");
            } else {
                log.atInfo().log("Metadata of episode %s has already been commited at the beginning of the branch", sequenceId);
            }
            if (shouldTagStart){
                log.atInfo().log("Creating tag %s", startName);
                doTagStart();
                log.atInfo().log("Creating the tag finished");
            } else {
                log.atInfo().log("Tag %s already present", startName);
            }
            body.run();
            if (shouldTagEnd){
                log.atInfo().log("Creating tag %s", endName);
                doTagEnd();
                log.atInfo().log("Creating the tag finished");
            } else {
                log.atInfo().log("Tag %s already present", endName);
            }
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

        //fixme ditto
        private void doTagEnd(){
            //todo invariant: on progress head; does this apply in other places too?
            workingCopy.createTag(endName);
            log.atFine().log("Tag %s created", endName);
            workingCopy.push(asList(progressName), true);
            log.atFine().log("Changes pushed");
            workingCopy.checkoutExisting(progressName);
            log.atFine().log("Switched back to sequence progress branch");
        }

    }

    @Override
    public void body() {
        var progress = new Progress();
        progress.init();
        progress.plan();
        progress.exec();
    }



}

