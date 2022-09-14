package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithOneReference;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TreeStructureEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesSubtask;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class SubtaskIncorporated<Id extends Comparable<Id>> extends BaseEntryWithOneReference<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Hierarchy,
    ReferencesSubtask<Id> { //todo ...added? as in "added to internal history"
    public SubtaskIncorporated(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt, @NonNull Id subtask) {
        super(session, happenedAt, subtask);
    }

    @Override
    protected String toStringReferenceAlias() {
        return "subtaskId";
    }

    @Override
    public Id getSubtaskId(){
        return getReference();
    }
}
