package com.github.filipmalczak.storyteller.api;

import com.github.filipmalczak.storyteller.api.story.Storyteller;

public interface StorytellerFactory<Config> {
    Storyteller create(Config config);
}
