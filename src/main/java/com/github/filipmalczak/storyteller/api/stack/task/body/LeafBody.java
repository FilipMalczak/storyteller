package com.github.filipmalczak.storyteller.api.stack.task.body;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;

@FunctionalInterface
public non-sealed interface LeafBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> extends TaskBody<Id, Definition, Type, NoSql>{
    void perform(ReadWriteStorage<NoSql> storage);
}
