package com.github.filipmalczak.storyteller.api.session.events;

public sealed interface SessionTaskEvent<TaskId extends Comparable<TaskId>> extends SessionEvent permits TaskOpened, TaskClosed, TaskFailed {
    TaskId getTaskId();
}
