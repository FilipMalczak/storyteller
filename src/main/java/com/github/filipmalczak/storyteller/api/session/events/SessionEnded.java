package com.github.filipmalczak.storyteller.api.session.events;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;


@EqualsAndHashCode(callSuper = true)
public final class SessionEnded extends BaseSessionEvent implements SessionLifecycleEvent, Joinpoint.End.Success {
    public SessionEnded(@NonNull String id, @NonNull ZonedDateTime happenedAt) {
        super(id, happenedAt);
    }
}