package com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesData;

/**
 * Entries that refer to a subtask of the task owning them.
 */
@DescribesData
public interface ReferencesSubtask<Id extends Comparable<Id>> extends ReferencesSingleTask<Id> {
    Id getSubtaskId();

    default Id getReference(){
        return getSubtaskId();
    }
}
