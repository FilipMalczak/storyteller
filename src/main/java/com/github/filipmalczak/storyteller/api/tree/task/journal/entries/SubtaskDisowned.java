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
public final class SubtaskDisowned<Id extends Comparable<Id>> extends AbstractReferencesSubtask<Id> {
    public SubtaskDisowned(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull Id disowmned) {
        super(session, happenedAt, disowmned);
    }

    public Id getDisownedSubtaskId(){
        return getReference();
    }
}
