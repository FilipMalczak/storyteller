package com.github.filipmalczak.storyteller.api.tree.task.body;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;

@FunctionalInterface
public interface LeafBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    void perform(ReadWriteStorage<NoSql> storage);
}
