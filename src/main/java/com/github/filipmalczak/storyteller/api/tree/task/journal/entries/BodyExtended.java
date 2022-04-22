package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.Session;
import lombok.NonNull;

import java.time.ZonedDateTime;

public final class BodyExtended extends AbstractJournalEntry {
    public BodyExtended(@NonNull Session session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
