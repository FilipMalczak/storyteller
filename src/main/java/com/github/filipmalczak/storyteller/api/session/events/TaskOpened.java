package com.github.filipmalczak.storyteller.api.session.events;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class TaskOpened<TaskId extends Comparable<TaskId>> extends BaseSessionEventWithTask<TaskId>
    implements SessionTaskEvent<TaskId>, Joinpoint.Start {
    public TaskOpened(@NonNull String id, @NonNull ZonedDateTime happenedAt, @NonNull TaskId taskId) {
        super(id, happenedAt, taskId);
    }
}
