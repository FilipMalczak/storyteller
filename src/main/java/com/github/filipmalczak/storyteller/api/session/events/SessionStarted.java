package com.github.filipmalczak.storyteller.api.session.events;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class SessionStarted extends SessionLifecycleEvent {
    public SessionStarted(@NonNull Session subject) {
        super(subject);
    }
}
