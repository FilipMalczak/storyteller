package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import lombok.NonNull;

import java.time.ZonedDateTime;

public final class NodeExtended extends AbstractJournalEntry {
    public NodeExtended(@NonNull Session session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
