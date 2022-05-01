package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

public interface ReferencesSubtask<Id extends Comparable<Id>> extends ReferencesSubtasks<Id> {
    default Id getReference(){
        return getReferences().get(0);
    }
}
