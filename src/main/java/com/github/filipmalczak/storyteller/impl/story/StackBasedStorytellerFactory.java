package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.StorytellerFactory;

public class StackBasedStorytellerFactory<NoSql> implements StorytellerFactory<NoSql, StackedExecutor<String, String, EpisodeType, NoSql>> {
    @Override
    public Storyteller<NoSql> create(StackedExecutor<String, String, EpisodeType, NoSql> executor) {
        return new StackBasedStoryteller(executor);
    }

}
