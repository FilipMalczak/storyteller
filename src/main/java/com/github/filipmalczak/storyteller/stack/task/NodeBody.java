package com.github.filipmalczak.storyteller.stack.task;

import com.github.filipmalczak.storyteller.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.storage.ReadStorage;

public non-sealed interface NodeBody<Id, Definition, Type extends Enum<Type> & TaskType> extends TaskBody<Id, Definition, Type> {
    void perform(StackedExecutor<Id, Definition, Type> executor, ReadStorage storage);
}
