package com.github.filipmalczak.storyteller.api.story;

@FunctionalInterface
public interface ActionBody<Arg> {
    void action(Arg arg);
}
