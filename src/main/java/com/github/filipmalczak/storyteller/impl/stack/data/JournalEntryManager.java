package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.stack.task.Task;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.JournalEntry;

import java.util.stream.Stream;

public interface JournalEntryManager<TaskId> {
    void record(Task<TaskId, ?, ?> task, JournalEntry entry);

    Stream<JournalEntry> findByTaskId(TaskId taskId);

    default Stream<JournalEntry> findByTask(Task<TaskId, ?, ?> task){
        return findByTaskId(task.getId());
    }
}
