package com.github.filipmalczak.storyteller.api.tree.task.body.handles;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

public interface Insight<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    ReadStorage<NoSql> into(Id id);

    default ReadStorage<NoSql> into(Task<Id, Definition, Type> subtask) {
        return into(subtask.getId());
    }
}
