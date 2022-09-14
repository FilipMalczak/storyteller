package com.github.filipmalczak.storyteller.api.session.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public final class SessionStarted extends BaseSessionEvent implements SessionLifecycleEvent, Joinpoint.Start {
    @NonNull String hostname;

    public SessionStarted(@NonNull String id, @NonNull ZonedDateTime happenedAt, @NonNull String hostname) {
        super(id, happenedAt);
        this.hostname = hostname;
    }
}
