package com.github.filipmalczak.storyteller.stack.task;

public interface IdGeneratorFactory<Definition, Id> {

    IdGenerator<Definition, Id> over(Definition definition);
}
