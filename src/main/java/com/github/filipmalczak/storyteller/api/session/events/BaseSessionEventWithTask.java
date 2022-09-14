package com.github.filipmalczak.storyteller.api.session.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public sealed abstract class BaseSessionEventWithTask<TaskId extends Comparable<TaskId>> permits TaskClosed, TaskFailed, TaskOpened {
    @NonNull String sessionId;
    @NonNull ZonedDateTime happenedAt;
    @NonNull TaskId taskId;
}
