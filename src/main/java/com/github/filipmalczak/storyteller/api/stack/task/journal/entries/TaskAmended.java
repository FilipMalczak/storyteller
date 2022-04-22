package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class TaskAmended extends AbstractJournalEntry {
    public TaskAmended(@NonNull Session session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
