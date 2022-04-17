package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ExceptionCaught extends AbstractJournalEntry {
    String message;
    String fullStackTrace;

    public ExceptionCaught(Session session, ZonedDateTime happenedAt, String message, String fullStackTrace) {
        super(session, happenedAt);
        this.message = message;
        this.fullStackTrace = fullStackTrace;
    }
}