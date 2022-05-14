package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithOneAndManyReferences;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TreeStructureEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesModifiedSubtasks;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesSingleTask;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesTasks;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class BodyChanged<Id extends Comparable<Id>> extends BaseEntryWithOneAndManyReferences<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Computations,
                ReferencesSingleTask<Id>, ReferencesTasks<Id>, ReferencesModifiedSubtasks.AddedAndRemoved<Id> {

    public BodyChanged(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt, @NonNull Id pivot, @NonNull List<Id> conflicts) {
        super(session, happenedAt, pivot, conflicts);
    }

    @Override
    protected String toStringReferencesAlias() {
        return "conflictingSubtasks";
    }

    @Override
    protected String toStringReferenceAlias() {
        return "pivot";
    }

    public Stream<Id> getConflictingSubtasks(){
        return getDecrement();
    }

    public Id getPivot(){ return getReference(); }

    @Override
    public Stream<Id> getIncrement() {
        return Stream.of(getReference());
    }

    @Override
    public Stream<Id> getDecrement() {
        return getReferences();
    }

}
