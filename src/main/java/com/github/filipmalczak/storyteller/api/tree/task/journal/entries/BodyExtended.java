package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithReferences;
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
public final class BodyExtended<Id extends Comparable<Id>> extends BaseEntryWithReferences<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Computations,
                ReferencesTasks<Id>, ReferencesModifiedSubtasks.Added<Id> {
    public BodyExtended(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> added) {
        super(session, happenedAt, added);
    }

    @Override
    protected String toStringReferencesAlias() {
        return "added";
    }

    @Override
    public Stream<Id> getIncrement(){
        return getReferences();
    }

    public Stream<Id> getAdded(){
        return getIncrement();
    }
}
