package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.session.Sessions;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;

public interface TaskTreeRoot<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    Sessions getSessions();
    Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body);
}
