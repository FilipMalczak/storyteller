package com.github.filipmalczak.storyteller.impl.jgit;

import com.github.filipmalczak.storyteller.api.StorytellerFactory;
import com.github.filipmalczak.storyteller.api.story.Storyteller;

public class JGitStorytelerFactory implements StorytellerFactory<JGitStorytellerConfig> {
    @Override
    public Storyteller create(JGitStorytellerConfig config) {
        return new JGitStoryteller(config.getStoryRoot(), config.getTempRoot());
    }
}
