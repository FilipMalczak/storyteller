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
public final class DefineSubtask extends AbstractJournalEntry {
    Task defined;

    public DefineSubtask(Session session, ZonedDateTime happenedAt, Task defined) {
        super(session, happenedAt);
        this.defined = defined;
    }
}
