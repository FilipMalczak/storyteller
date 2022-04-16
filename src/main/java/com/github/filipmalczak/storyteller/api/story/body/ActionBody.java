package com.github.filipmalczak.storyteller.api.story.body;

@FunctionalInterface
public interface ActionBody<Arg> {
    void action(Arg arg);
}
