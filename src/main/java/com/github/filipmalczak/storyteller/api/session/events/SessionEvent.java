package com.github.filipmalczak.storyteller.api.session.events;

import java.time.ZonedDateTime;

public sealed interface SessionEvent permits SessionLifecycleEvent, SessionTaskEvent {
    String getSessionId();
    ZonedDateTime getHappenedAt();
}
