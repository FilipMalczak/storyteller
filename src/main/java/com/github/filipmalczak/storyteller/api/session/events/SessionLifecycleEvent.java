package com.github.filipmalczak.storyteller.api.session.events;

public sealed interface SessionLifecycleEvent extends SessionEvent permits SessionStarted, SessionEnded, SessionKilled {
}
