package com.github.filipmalczak.storyteller.api.session.listener;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import lombok.Value;
import lombok.extern.flogger.Flogger;

import java.util.logging.Level;

@Flogger
@Value
public class LoggingSessionListener<T extends SessionEvent> implements SessionListener<T> {
    Level level = Level.FINE;

    @Override
    public void on(T event) {
        log.at(level).log("Session event: %s", event);
    }
}
