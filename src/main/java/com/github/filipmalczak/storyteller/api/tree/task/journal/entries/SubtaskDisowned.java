package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntryWithOneReference;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TreeStructureEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesSingleTask;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class SubtaskDisowned<Id extends Comparable<Id>> extends BaseEntryWithOneReference<Id>
    implements JournalEntry, TreeStructureEvent, TaskStage.Hierarchy,
    ReferencesSingleTask<Id> { //todo ReferencesModifiedSubtasks.Added?
    public SubtaskDisowned(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt, @NonNull Id disowmned) {
        super(session, happenedAt, disowmned);
    }

    @Override
    protected String toStringReferenceAlias() {
        return "disownedSubtaskId";
    }

    public Id getDisownedSubtaskId(){
        return getReference();
    }
}
