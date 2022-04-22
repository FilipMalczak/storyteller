package com.github.filipmalczak.storyteller.api.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;

//todo rename to PersistentTaskTree
public interface StackedExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    //todo rename to executeLinear
    Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body);

    //todo rename to executeParallel, add integrate as parameter of body
    Task<Id, Definition, Type> chooseNextSteps(Definition definition, Type type, ChoiceBody<Id, Definition, Type, NoSql> body);

    //todo startSession?
//    void endSession(); //todo should be part of PersistentTaskRoot extends PersistentTaskTree
}
