package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

public interface TaskTreeFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql, Config> {
    TaskTreeRoot<Id, Definition, Type, NoSql> create(Config config);
}
