package com.github.filipmalczak.storyteller.api;

import com.github.filipmalczak.storyteller.api.story.Storyteller;

public interface StorytellerSetup<NoSql, Config> {
    //todo deprec
    Storyteller<NoSql> create(Config config);
}
