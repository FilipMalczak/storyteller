package com.github.filipmalczak.storyteller.api.tree.task.body.handles;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.tree.task.Task;

public interface Insight<Id extends Comparable<Id>, NoSql> {
    ReadStorage<NoSql> into(Id id);

    default ReadStorage<NoSql> into(Task<Id, ?, ?> subtask) {
        return into(subtask.getId());
    }
}
