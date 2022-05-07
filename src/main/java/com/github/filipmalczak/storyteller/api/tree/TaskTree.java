package com.github.filipmalczak.storyteller.api.tree;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.ParallelNodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Insight;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public interface TaskTree<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    //todo extract
    interface IncorporationFilter<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
        Set<Task<Id, Definition, Type>> chooseIncorporated(Set<Task<Id, Definition, Type>> subtasks, Insight<Id, NoSql> insight);
    }

    Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body);

    Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body, IncorporationFilter<Id, Definition,Type, NoSql> filter);

    Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body);
}
