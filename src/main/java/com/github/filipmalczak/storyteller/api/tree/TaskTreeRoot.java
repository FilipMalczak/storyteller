package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

public interface TaskTreeRoot<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> extends TaskTree<Id, Definition, Type, NoSql> {
    Sessions getSessions();
}
