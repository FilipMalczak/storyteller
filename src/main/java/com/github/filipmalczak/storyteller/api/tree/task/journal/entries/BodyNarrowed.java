package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BodyNarrowed<Id extends Comparable<Id>> extends AbstractReferencesSubtasks<Id> {
    public BodyNarrowed(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> disappeared) {
        super(session, happenedAt, disappeared);
    }

    public List<Id> getDisappeared(){
        return getReferences();
    }
}
