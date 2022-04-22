package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.Session;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class SubtaskDefined extends AbstractReferencesSubtasks {
    public SubtaskDefined(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Task> referenced) {
        super(session, happenedAt, referenced);
    }

    public Task getDefinedSubtask(){
        return getReferenced().get(0);
    }
}
