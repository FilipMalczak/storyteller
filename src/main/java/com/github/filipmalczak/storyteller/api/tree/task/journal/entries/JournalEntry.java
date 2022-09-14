package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import java.time.ZonedDateTime;

public sealed interface JournalEntry permits
    SubtaskDefined, TaskStarted,
    InstructionsRan, InstructionsSkipped, BodyExecuted,
    ExceptionCaught, TaskInterrupted,
    BodyChanged, BodyExtended, BodyNarrowed, TaskAmended,
    SubtaskDisowned, TaskOrphaned,
    ParallelNodeRefiltered, ParallelNodeInflated, ParallelNodeDeflated, ParallelNodeAugmented,
    SubtaskIncorporated, TaskEnded {

    LazySession getSession();
    ZonedDateTime getHappenedAt();
}
