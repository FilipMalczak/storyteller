package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
import com.github.filipmalczak.storyteller.stack.task.Task;
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
public final class IntegrateSubtask extends AbstractJournalEntry {
    Task integrated;

    public IntegrateSubtask(Session session, ZonedDateTime happenedAt, Task integrated) {
        super(session, happenedAt);
        this.integrated = integrated;
    }
}
