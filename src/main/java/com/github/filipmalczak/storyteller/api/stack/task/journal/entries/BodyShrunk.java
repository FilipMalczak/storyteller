package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class BodyShrunk extends AbstractReferencesSubtasks {
    public BodyShrunk(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Task> referenced) {
        super(session, happenedAt, referenced);
    }

    public List<Task> getDisappeared(){
        return getReferenced();
    }
}
