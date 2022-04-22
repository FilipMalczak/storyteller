package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.task.Task;

import java.util.List;

public interface ReferencesSubtask {
    List<Task> getReferenced();
}
