package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;

import java.time.ZonedDateTime;

public sealed interface JournalEntry permits
    SubtaskDefined, TaskStarted,
    InstructionsRan, InstructionsSkipped, BodyExecuted,
    ExceptionCaught, TaskInterrupted,
    BodyChanged, BodyExtended, BodyNarrowed, TaskAmended,
    SubtaskDisowned, TaskOrphaned,
    ParallelNodeRefiltered, ParallelNodeInflated, ParallelNodeDeflated, ParallelNodeAugmented,
    SubtaskIncorporated, TaskEnded {

    Session getSession();
    ZonedDateTime getHappenedAt();
}
