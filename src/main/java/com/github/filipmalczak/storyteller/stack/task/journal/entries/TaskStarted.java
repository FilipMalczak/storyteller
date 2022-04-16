package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class TaskStarted extends AbstractJournalEntry {
    public TaskStarted(Session session, ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
