package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;

import java.time.ZonedDateTime;

public sealed abstract class Perform extends AbstractJournalEntry permits RunIntructions, SkipAlreadyExecuted {
    public Perform(Session session, ZonedDateTime happenedAt) {
        super(session, happenedAt);
    }
}
