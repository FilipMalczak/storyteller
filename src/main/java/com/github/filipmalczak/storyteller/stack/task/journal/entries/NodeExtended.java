package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;

import java.time.ZonedDateTime;

public final class NodeExtended extends AbstractJournalEntry {
    public NodeExtended(Session session, ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
