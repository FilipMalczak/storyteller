package com.github.filipmalczak.storyteller.api.session.listener;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;

public interface SessionListener<T extends SessionEvent> {
    void on(T event);
}
