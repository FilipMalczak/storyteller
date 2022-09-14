package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.BodyExecutionEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class InstructionsSkipped extends BaseEntry
    implements JournalEntry, BodyExecutionEvent, TaskStage.Computations {
    public InstructionsSkipped(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
