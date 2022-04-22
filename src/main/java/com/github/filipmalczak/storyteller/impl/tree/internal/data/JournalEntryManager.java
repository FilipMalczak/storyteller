package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.util.stream.Stream;

public interface JournalEntryManager<TaskId> {
    record TaskEntry<TaskId> (Task<TaskId, ?, ?> task, JournalEntry entry) {}

    static <TaskId> TaskEntry<TaskId> taskEntry(Task<TaskId, ?, ?> task, JournalEntry entry){
        return new TaskEntry<>(task, entry);
    }

    void record(TaskEntry<TaskId>... entries);

    default void record(Task<TaskId, ?, ?> task, JournalEntry entry){
        record(taskEntry(task, entry));
    }

    Stream<JournalEntry> findByTaskId(TaskId taskId);

    default Stream<JournalEntry> findByTask(Task<TaskId, ?, ?> task){
        return findByTaskId(task.getId());
    }
}
