package com.github.filipmalczak.storyteller.api.story;

public interface Storyteller {
    void tell(String storyName, ActionBody<ArcClosure> arcClosure);
}
