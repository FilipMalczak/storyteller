package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage;

import com.github.filipmalczak.storyteller.impl.jgit.episodes.identity.EpisodeId;
import com.github.filipmalczak.storyteller.impl.jgit.storage.WorkingCopy;
import com.google.common.flogger.FluentLogger;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.PROGRESS;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.RefNames.buildRefName;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.Safeguards.invariant;

/**
 * Whenever docs of a stages behaviour use verbs like "defines", "starts", etc it actually means "looks up for proof
 * of defining, if its missing, perform defining and persist its proof in GIT". In some cases these details are given
 * explicitly - that happens when the rules of looking the proof up, persisting it and realigning the repository for
 * further storytelling are non-trivial.
 */
public interface Stage {
    //lomboks @Flogger wont work in interfaces
    FluentLogger log = FluentLogger.forEnclosingClass();

    /**
     * Each stage operates wherever it wants (usually on scope progress branch), but it always exits on scopes progress branch
     * @return ID of the enclosing episode; never null (each episode has ID and each stage runs within episode)
     */
    EpisodeId getScope();
    WorkingCopy getWorkingCopy();
    void setWorkingCopy(WorkingCopy workingCopy);
    void body();

    default void run(WorkingCopy workingCopy){
        log.atFine().log("Running stage of type %s in scope of episode %s", this.getClass().getSimpleName(), getScope());
        var prev = getWorkingCopy();
        log.atFine().log("Stored working copy %s", prev);
        setWorkingCopy(workingCopy);
        log.atFine().log("Switched working copy to %s", workingCopy);
        try {
            var branch = buildRefName(getScope(), PROGRESS);
            body();
//            invariant(branch.equals(getWorkingCopy().head()), "Stage must exit on its scopes' progress branch!");
            getWorkingCopy().checkoutExisting(branch);
        } finally {
            setWorkingCopy(prev);
        }
    }
}
