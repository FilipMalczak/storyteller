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
public final class BodyExecuted extends BaseEntry
    implements JournalEntry, BodyExecutionEvent, TaskStage.Computations {
    public BodyExecuted(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
