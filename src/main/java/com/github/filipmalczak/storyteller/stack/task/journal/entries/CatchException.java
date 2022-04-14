package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
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
public final class CatchException extends AbstractJournalEntry {
    String message;
    String fullStackTrace;

    public CatchException(Session session, ZonedDateTime happenedAt, String message, String fullStackTrace) {
        super(session, happenedAt);
        this.message = message;
        this.fullStackTrace = fullStackTrace;
    }
}
