package com.github.filipmalczak.storyteller.api.session.listener;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.flogger.Flogger;

import java.util.logging.Level;

import static java.util.logging.Level.INFO;

@Flogger
@Value
@AllArgsConstructor
public class LoggingSessionListener<T extends SessionEvent> implements SessionListener<T> {
    Level level;

    public LoggingSessionListener() {
        this(INFO);
    }

    @Override
    public void on(T event) {
        log.at(level).log("Session event: %s", event);
    }
}
