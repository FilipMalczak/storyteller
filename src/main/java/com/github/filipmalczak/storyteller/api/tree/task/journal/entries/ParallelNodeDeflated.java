package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithReferences;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TreeStructureEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesModifiedSubtasks;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesTasks;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class ParallelNodeDeflated<Id extends Comparable<Id>> extends BaseEntryWithReferences<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Planning,
                ReferencesTasks<Id>, ReferencesModifiedSubtasks.Removed<Id> {
    public ParallelNodeDeflated(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> disappeared) {
        super(session, happenedAt, disappeared);
    }

    @Override
    protected String toStringReferencesAlias() {
        return "disappeared";
    }

    public Stream<Id> getDisappeared(){
        return getDecrement();
    }

    @Override
    public Stream<Id> getDecrement() {
        return getReferences();
    }
}
