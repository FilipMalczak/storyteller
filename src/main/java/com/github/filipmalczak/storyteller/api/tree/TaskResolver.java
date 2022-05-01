package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

import java.util.Optional;

public interface TaskResolver<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    Optional<Task<Id, Definition, Type>> resolve(Id id);
}
