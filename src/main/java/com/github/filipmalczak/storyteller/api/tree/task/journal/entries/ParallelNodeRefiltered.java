package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ParallelNodeRefiltered<Id extends Comparable<Id>> extends AbstractJournalEntry {
    public ParallelNodeRefiltered(@NonNull Session session, @NonNull ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
