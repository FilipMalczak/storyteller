package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public sealed abstract class AbstractJournalEntry implements JournalEntry permits AbstractReferencesSubtasks, Augmented, BodyExtended, Deflated, ExceptionCaught, Inflated, OrderingLoosened, OrderingTightened, Reordered, TaskAmended, TaskEnded, TaskInterrupted, TaskOrphaned, TaskPerformed, TaskStarted {
    @NonNull Session session;
    @NonNull ZonedDateTime happenedAt;
}
