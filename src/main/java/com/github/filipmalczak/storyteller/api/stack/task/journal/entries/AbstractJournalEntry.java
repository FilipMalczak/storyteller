package com.github.filipmalczak.storyteller.api.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.api.stack.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@EqualsAndHashCode
@ToString
public sealed abstract class AbstractJournalEntry implements JournalEntry permits AbstractSubtaskJournalEntry, DisownedByParent, ExceptionCaught, NodeExtended, TaskEnded, TaskPerformed, TaskStarted {
    Session session;
    ZonedDateTime happenedAt;
}
