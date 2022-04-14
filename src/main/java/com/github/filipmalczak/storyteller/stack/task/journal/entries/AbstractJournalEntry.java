package com.github.filipmalczak.storyteller.stack.task.journal.entries;

import com.github.filipmalczak.storyteller.stack.Session;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public sealed abstract class AbstractJournalEntry implements JournalEntry permits CatchException, DefineSubtask, EndTask, IntegrateSubtask, NodeExtended, Perform, StartTask {
    Session session;
    ZonedDateTime happenedAt;
}
