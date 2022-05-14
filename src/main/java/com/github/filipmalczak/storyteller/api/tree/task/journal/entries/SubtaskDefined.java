package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithOneReference;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.EntryPersistence;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesSubtask;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class SubtaskDefined<Id extends Comparable<Id>> extends BaseEntryWithOneReference<Id>
    implements JournalEntry, EntryPersistence.LifecycleEvent, TaskStage.Hierarchy,
            ReferencesSubtask<Id> {
    public SubtaskDefined(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt, @NonNull Id definedSubtask) {
        super(session, happenedAt, definedSubtask);
    }

    @Override
    protected String toStringReferenceAlias() {
        return "subtaskId";
    }

    @Override
    public Id getSubtaskId() {
        return getReference();
    }
}
