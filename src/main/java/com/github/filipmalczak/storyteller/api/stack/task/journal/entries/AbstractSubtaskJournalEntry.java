package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public sealed class AbstractSubtaskJournalEntry extends AbstractJournalEntry implements ReferencesSubtask permits BodyChanged, SubtaskDefined, SubtaskDisowned {
    Task referenced;

    public AbstractSubtaskJournalEntry(Session session, ZonedDateTime happenedAt, Task referenced) {
        super(session, happenedAt);
        this.referenced = referenced;
    }
}
