package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ExceptionCaught extends AbstractJournalEntry {
    @NonNull String className;
    String message; //nullable, as not every exception has a message
    @NonNull String fullStackTrace;

    public ExceptionCaught(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull String className, String message, @NonNull String fullStackTrace) {
        super(session, happenedAt);
        this.className = className;
        this.message = message;
        this.fullStackTrace = fullStackTrace;
    }
}
