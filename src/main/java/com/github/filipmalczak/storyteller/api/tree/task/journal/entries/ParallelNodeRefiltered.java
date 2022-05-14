package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithReferenceListDiff;
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
public final class ParallelNodeRefiltered<Id extends Comparable<Id>> extends BaseEntryWithReferenceListDiff<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Planning,
                ReferencesTasks<Id>, ReferencesModifiedSubtasks.AddedAndRemoved<Id> {
    public ParallelNodeRefiltered(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> appeared, @NonNull List<Id> disappeared) {
        super(session, happenedAt, appeared, disappeared);

    }

    @Override
    protected String toStringIncrementAlias() {
        return "appeared";
    }

    @Override
    protected String toStringDecrementAlias() {
        return "disappeared";
    }

    public Stream<Id> getAppeared(){
        return getIncrement();
    }

    public Stream<Id> getDisappeared(){
        return getDecrement();
    }
}
