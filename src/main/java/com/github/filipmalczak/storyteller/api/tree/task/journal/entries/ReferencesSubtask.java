package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.task.Task;

import java.util.List;

public interface ReferencesSubtask {
    List<Task> getReferenced();
}
