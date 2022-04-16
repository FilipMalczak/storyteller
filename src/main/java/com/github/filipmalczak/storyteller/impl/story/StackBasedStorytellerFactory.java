package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.StorytellerFactory;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;

public class StackBasedStorytellerFactory implements StorytellerFactory<StackedExecutor<String, String, EpisodeType>> {
    @Override
    public Storyteller create(StackedExecutor<String, String, EpisodeType> executor) {
        return new StackBasedStoryteller(executor);
    }

}
