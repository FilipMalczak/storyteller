package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DisownedByParent extends AbstractJournalEntry {
    public DisownedByParent(Session session, ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
