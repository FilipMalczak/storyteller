package com.github.filipmalczak.storyteller.api.story.body;

public interface ResearchBody<Key, Arg> {
    void research(Key key, Arg arg);
}
