package com.github.filipmalczak.storyteller.impl.jgit.episodes.impl.stage.utils;

import com.github.filipmalczak.storyteller.api.story.ActionBody;

@FunctionalInterface
public interface StageBody extends Runnable {
    static <T> StageBody runAction(ActionBody<T> action, T closure){
        return () -> action.action(closure);
    }
}
