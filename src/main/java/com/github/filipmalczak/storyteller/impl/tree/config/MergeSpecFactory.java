package com.github.filipmalczak.storyteller.impl.tree.config;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

@FunctionalInterface
public interface MergeSpecFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    MergeSpec<Definition, Type> forParallelNode(Task<Id, Definition, Type> parallelNode);
}
