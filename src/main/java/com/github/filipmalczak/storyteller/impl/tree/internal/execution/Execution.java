package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;

public interface Execution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    ExecutionContext<Id, Definition, Type> context();
    void run();
}
