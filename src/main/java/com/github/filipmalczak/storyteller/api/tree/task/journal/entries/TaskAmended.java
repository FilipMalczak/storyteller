package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.FinalizingEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class TaskAmended extends BaseEntry
    implements JournalEntry, FinalizingEvent, TaskStage.Computations {
    public TaskAmended(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
