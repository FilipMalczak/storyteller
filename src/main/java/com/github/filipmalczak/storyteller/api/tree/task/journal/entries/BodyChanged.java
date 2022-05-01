package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BodyChanged<Id extends Comparable<Id>> extends AbstractReferencesSubtasks<Id> {
    public BodyChanged(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> conflicts) {
        super(session, happenedAt, conflicts);
    }

    public List<Id> getConflictingSubtasks(){
        return getReferences();
    }

}
