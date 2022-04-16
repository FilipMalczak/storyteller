package com.github.filipmalczak.storyteller.api.stack.task;

public interface IdGenerator<Id, Definition, Type extends Enum<Type> & TaskType> {
    Definition definition();
    Type type();

    /**
     * IDs cannot be fully random - it must be possible to check whether another ID could have been generated for the
     * same (definition, type) pair.
     */
    Id generate();

    boolean canReuse(Id id);
}
