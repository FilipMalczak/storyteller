package com.github.filipmalczak.storyteller.api.story;

public interface ResearchBody<Key, Arg> {
    void research(Key key, Arg arg);
}
