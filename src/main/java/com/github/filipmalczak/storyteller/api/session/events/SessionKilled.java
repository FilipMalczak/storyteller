package com.github.filipmalczak.storyteller.api.session.events;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class SessionKilled extends BaseSessionEvent implements SessionLifecycleEvent, Joinpoint.End.Failure {
    public SessionKilled(@NonNull String id, @NonNull ZonedDateTime happenedAt) {
        super(id, happenedAt);
    }
}
