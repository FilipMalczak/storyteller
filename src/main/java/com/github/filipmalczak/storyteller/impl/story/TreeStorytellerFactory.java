package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.StorytellerFactory;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;

public class TreeStorytellerFactory<NoSql> implements StorytellerFactory<NoSql, TaskTreeRoot<String, String, EpisodeType, NoSql>> {
    @Override
    public Storyteller<NoSql> create(TaskTreeRoot<String, String, EpisodeType, NoSql> executor) {
        return new TreeStoryteller(executor);
    }

}
