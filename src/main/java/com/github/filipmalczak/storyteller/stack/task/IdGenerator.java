package com.github.filipmalczak.storyteller.stack.task;

public interface IdGenerator<Definition, Id> {
    Definition definition();

    Id generate();

    boolean canReuse(Id id);
}
