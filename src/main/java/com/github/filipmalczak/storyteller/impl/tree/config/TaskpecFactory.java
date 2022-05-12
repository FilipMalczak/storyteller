package com.github.filipmalczak.storyteller.impl.tree.config;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

@FunctionalInterface
public interface TaskpecFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    TaskSpec<Definition, Type> forParallelNode(Task<Id, Definition, Type> parallelNode);
}
