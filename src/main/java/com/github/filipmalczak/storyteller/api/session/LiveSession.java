package com.github.filipmalczak.storyteller.api.session;

import com.github.filipmalczak.storyteller.api.common.ActionBody;
import com.github.filipmalczak.storyteller.api.story.Storyteller;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

public non-sealed interface LiveSession<TaskId extends Comparable<TaskId>, Definition, Type extends Enum<Type> & TaskType, NoSql> extends Session<TaskId>, AutoCloseable {
    void action(ActionBody<TaskTreeRoot<TaskId, Definition, Type, NoSql>> body);

    /**
     * Should throw IllegalStateException if session wasn't closed yet.
     */
    PastSession<TaskId, NoSql> forPostMortem();

    //override to remove exception from signature
    void close();

    boolean isClosed();
}
