package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;

import java.time.ZonedDateTime;

public sealed interface JournalEntry permits AbstractJournalEntry {
    Session getSession();
    ZonedDateTime getHappenedAt();
}
