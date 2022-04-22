package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.Session;

import java.time.ZonedDateTime;

//todo does this require id and definition as generics?
public sealed interface JournalEntry permits AbstractJournalEntry {
    //todo add session and timestamp
    Session getSession();
    ZonedDateTime getHappenedAt();
}
