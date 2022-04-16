package com.github.filipmalczak.storyteller.api.story.body;

@FunctionalInterface
public interface StructureBody<Struct, Arg> {
    void action(Struct struct, Arg arg);
}
