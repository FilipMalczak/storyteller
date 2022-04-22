package com.github.filipmalczak.storyteller.api.tree.task.id;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

public interface IdGeneratorFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {

    IdGenerator<Id, Definition, Type> over(Definition definition, Type type);
}
