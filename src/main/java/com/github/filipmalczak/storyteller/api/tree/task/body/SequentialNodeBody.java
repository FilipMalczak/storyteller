package com.github.filipmalczak.storyteller.api.tree.task.body;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

//todo look up other bodies, mark as functional
//todo rename to LinearNodeBody
@FunctionalInterface
public interface SequentialNodeBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    void perform(TaskTree<Id, Definition, Type, NoSql> executor, ReadStorage storage);
}
