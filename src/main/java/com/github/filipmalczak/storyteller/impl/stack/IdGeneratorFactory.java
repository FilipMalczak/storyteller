package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.stack.task.TaskType;

public interface IdGeneratorFactory<Id, Definition, Type extends Enum<Type> & TaskType> {

    IdGenerator<Id, Definition, Type> over(Definition definition, Type type);
}
