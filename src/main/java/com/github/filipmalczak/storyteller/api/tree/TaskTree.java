package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.ParallelNodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.SequentialNodeBody;

public interface TaskTree<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    Task<Id, Definition, Type> execute(Definition definition, Type type, SequentialNodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> execute(Definition definition, Type type, ParallelNodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body);

    //fixme this name is so awkward...
    default Task<Id, Definition, Type> chooseBranchToProceed(Definition definition, Type type, ChoiceBody<Id, Definition, Type, NoSql> body) {
        return execute(definition, type, (e, s, i, incorporate) -> incorporate.subtask(body.makeChoice(e, s, i)));
    }
}
