package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public sealed class AbstractReferencesSubtasks<Id extends Comparable<Id>> extends AbstractJournalEntry implements ReferencesSubtasks<Id> permits AbstractReferencesSubtask, BodyChanged, BodyNarrowed {
    @NonNull List<Id> references;

    public AbstractReferencesSubtasks(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> references) {
        super(session, happenedAt);
        this.references = references;
    }
}
