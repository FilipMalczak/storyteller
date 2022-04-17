package com.github.filipmalczak.storyteller.api.stack.task.body;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.storage.ReadStorage;

@FunctionalInterface
public non-sealed interface NodeBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> extends TaskBody<Id, Definition, Type, NoSql> {
    void perform(StackedExecutor<Id, Definition, Type, NoSql> executor, ReadStorage storage);
}
