package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
import com.github.filipmalczak.storyteller.stack.task.Task;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class SubtaskDefined extends AbstractSubtaskJournalEntry {
    public SubtaskDefined(Session session, ZonedDateTime happenedAt, Task referred) {
        super(session, happenedAt, referred);
    }

    public Task getDefinedSubtask(){
        return getReferenced();
    }
}
