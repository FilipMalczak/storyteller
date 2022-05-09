package com.github.filipmalczak.storyteller.api.session.listener;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.flogger.Flogger;

import java.util.logging.Level;

import static java.util.logging.Level.INFO;

@Flogger
@Value
@AllArgsConstructor
public class LoggingJournalListener<T extends JournalEntry> implements JournalListener<T> {
    Level level;

    public LoggingJournalListener() {
        this(INFO);
    }

    @Override
    public void on(Task owner, T entry) {
        log.at(level).log("New entry for '%s' of type %s: %s (task ID: %s)", owner.getDefinition(), owner.getType(), entry, owner.getId());
    }
}
