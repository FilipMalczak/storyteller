package com.github.filipmalczak.storyteller.api.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;

public interface StackedExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body);

//    Task<Id, Definition, Type> chooseNextSteps(Definition, Type, ChoiceBody<Id, Definition, Type, NoSql> body);
}
