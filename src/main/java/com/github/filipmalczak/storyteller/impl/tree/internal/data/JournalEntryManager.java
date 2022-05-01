package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.util.stream.Stream;

public interface JournalEntryManager<Id extends Comparable<Id>> {
    record TaskEntry<Id extends Comparable<Id>> (Task<Id, ?, ?> task, JournalEntry entry) {}

    static <Id extends Comparable<Id>> TaskEntry<Id> taskEntry(Task<Id, ?, ?> task, JournalEntry entry){
        return new TaskEntry<>(task, entry);
    }

    void record(TaskEntry<Id>... entries);

    default void record(Task<Id, ?, ?> task, JournalEntry entry){
        record(taskEntry(task, entry));
    }

    Stream<JournalEntry> findById(Id taskId);

    default Stream<JournalEntry> findByTask(Task<Id, ?, ?> task){
        return findById(task.getId());
    }
}
