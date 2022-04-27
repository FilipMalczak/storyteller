package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class TaskInterrupted extends AbstractJournalEntry {
    public TaskInterrupted(@NonNull Session session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
