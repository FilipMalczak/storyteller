package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;

//todo rename to PersistentTaskTree
public interface TaskTree<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    Task<Id, Definition, Type> executeSequential(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> executeSequential(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body);

    //todo rename to executeParallel, add integrate as parameter of body
    Task<Id, Definition, Type> chooseBranch(Definition definition, Type type, ChoiceBody<Id, Definition, Type, NoSql> body);
}
