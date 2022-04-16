package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
import com.github.filipmalczak.storyteller.stack.task.Task;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BodyChanged extends AbstractSubtaskJournalEntry {
    public Task getConflictingSubtask(){
        return getReferenced();
    }

    public BodyChanged(Session session, ZonedDateTime happenedAt, Task referenced) {
        super(session, happenedAt, referenced);
    }
}
