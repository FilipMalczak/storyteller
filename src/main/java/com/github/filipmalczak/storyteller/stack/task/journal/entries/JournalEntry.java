package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;

import java.time.ZonedDateTime;

//todo does this require id and definition as generics?
public sealed interface JournalEntry permits AbstractJournalEntry {
    //todo add session and timestamp
    Session getSession();
    ZonedDateTime getHappenedAt();
}
