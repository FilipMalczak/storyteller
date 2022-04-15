package com.github.filipmalczak.storyteller.stack.task;

import com.github.filipmalczak.storyteller.stack.task.TaskType;

public interface IdGenerator<Id, Definition, Type extends Enum<Type> & TaskType> {
    Definition definition();
    Type type();

    Id generate();

    boolean canReuse(Id id);
}
