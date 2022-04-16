package com.github.filipmalczak.storyteller.api.story;

public interface StorytellerFactory<Config> {
    Storyteller create(Config config);
}
