package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.tree.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public sealed abstract class AbstractJournalEntry implements JournalEntry permits AbstractReferencesSubtasks, BodyExtended, TaskOrphaned, ExceptionCaught, TaskAmended, TaskEnded, TaskInterrupted, TaskPerformed, TaskStarted {
    @NonNull Session session;
    @NonNull ZonedDateTime happenedAt;
}
