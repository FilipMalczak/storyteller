package com.github.filipmalczak.storyteller.stack.task;

//todo this is an impl interface
public interface DefinitionSerializer<Definition> {
    String serialize(Definition definition);

    Definition deserialize(String serialized);
}
