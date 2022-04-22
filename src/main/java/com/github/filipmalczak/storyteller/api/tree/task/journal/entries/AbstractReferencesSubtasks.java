package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.Session;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public sealed class AbstractReferencesSubtasks extends AbstractJournalEntry implements ReferencesSubtask permits BodyChanged, BodyShrunk, ChoiceWasMade, SubtaskDefined, SubtaskDisowned, SubtaskIncorporated {
    @NonNull List<Task> referenced;

    public AbstractReferencesSubtasks(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull List<Task> referenced) {
        super(session, happenedAt);
        this.referenced = referenced;
    }
}
