package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithReferences;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.EntryPersistence;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TreeStructureEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesModifiedSubtasks;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesTasks;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class BodyNarrowed<Id extends Comparable<Id>> extends BaseEntryWithReferences<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Computations,
                ReferencesTasks<Id>, ReferencesModifiedSubtasks.Removed<Id> {
    public BodyNarrowed(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> removed) {
        super(session, happenedAt, removed);
    }

    @Override
    protected String toStringReferencesAlias() {
        return "removed";
    }

    @Override
    public Stream<Id> getDecrement() {
        return getReferences();
    }

    public Stream<Id> getRemoved(){
        return getDecrement();
    }
}
