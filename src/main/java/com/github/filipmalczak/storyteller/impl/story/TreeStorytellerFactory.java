package com.github.filipmalczak.storyteller.impl.story;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.story.StorytellerFactory;

public class TreeStorytellerFactory<NoSql> implements StorytellerFactory<NoSql, TaskTree<String, String, EpisodeType, NoSql>> {
    @Override
    public Storyteller<NoSql> create(TaskTree<String, String, EpisodeType, NoSql> executor) {
        return new TreeStoryteller(executor);
    }

}
