package com.github.filipmalczak.storyteller.api.story;

public interface StorytellerFactory<NoSql, Config> {
    Storyteller<NoSql> create(Config config);
}
