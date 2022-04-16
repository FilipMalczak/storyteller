package com.github.filipmalczak.storyteller.api.stack.task.id;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;

public interface IdGeneratorFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {

    IdGenerator<Id, Definition, Type> over(Definition definition, Type type);
}
