package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BodyChanged extends AbstractSubtaskJournalEntry {
    public Task getConflictingSubtask(){
        return getReferenced();
    }

    public BodyChanged(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull Task referenced) {
        super(session, happenedAt, referenced);
    }
}
