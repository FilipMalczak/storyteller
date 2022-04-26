package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.ParallelNodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.SequentialNodeBody;

public interface TaskTree<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    //todo these should all be just execute
    Task<Id, Definition, Type> executeSequential(Definition definition, Type type, SequentialNodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> executeParallel(Definition definition, Type type, ParallelNodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> executeSequential(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body);

    //todo rename to executeParallel, add integrate as parameter of body
    default Task<Id, Definition, Type> chooseBranch(Definition definition, Type type, ChoiceBody<Id, Definition, Type, NoSql> body) {
        return executeParallel(definition, type, (e, s, i, incorporate) -> incorporate.subtask(body.makeChoice(e, s, i)));
    }
}
