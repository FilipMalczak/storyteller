package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.JournalledEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.base.BaseEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TaskStage;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.TreeStructureEvent;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@JournalledEvent
public final class ExceptionCaught extends BaseEntry
    implements JournalEntry, TreeStructureEvent, TaskStage.Hierarchy {
    @NonNull String className;
    String message; //nullable, as not every exception has a message
    @NonNull String fullStackTrace;

    public ExceptionCaught(@NonNull com.github.filipmalczak.storyteller.api.session.Session session, @NonNull ZonedDateTime happenedAt, @NonNull String className, String message, @NonNull String fullStackTrace) {
        super(session, happenedAt);
        this.className = className;
        this.message = message;
        this.fullStackTrace = fullStackTrace;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"{" +
            "session=" + getSession().getId() +
            ", happenedAt=" + getHappenedAt() +
            ", className='" + className + '\'' +
            ", message='" + message + '\'' +
            ", fullStackTrace.length=" + fullStackTrace.length() +
            '}';
    }
}
