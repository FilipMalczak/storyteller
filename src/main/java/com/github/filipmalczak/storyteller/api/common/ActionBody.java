package com.github.filipmalczak.storyteller.api.common;

@FunctionalInterface
public interface ActionBody<Arg> {
    void action(Arg arg);
}
