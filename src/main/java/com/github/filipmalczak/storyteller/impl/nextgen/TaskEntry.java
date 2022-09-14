package com.github.filipmalczak.storyteller.impl.nextgen;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

public record TaskEntry<Id extends Comparable<Id>>(Task<Id, ?, ?> task, JournalEntry entry) {

    static <Id extends Comparable<Id>> TaskEntry<Id> taskEntry(Task<Id, ?, ?> task, JournalEntry entry){
        return new TaskEntry<>(task, entry);
    }
}
