package com.github.filipmalczak.storyteller.api.stack;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;

public interface StackedExecutorFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, Config> {
    StackedExecutor<Id, Definition, Type> create(Config config);
}
