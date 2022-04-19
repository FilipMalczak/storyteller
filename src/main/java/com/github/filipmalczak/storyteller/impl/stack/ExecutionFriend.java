package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.JournalEntry;

import java.util.Optional;

//todo look through generics and check where Id specialization is really needed
public interface ExecutionFriend<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    void recordInParent(JournalEntry entry);
    void disownExpectedUpTheTrace();
    void setId(Id id);
    JournalEntryFactory journalEntryFactory();
    IdGenerator<Id, Definition, Type> idGenerator();
    Optional<Task<Id, Definition, Type>> findTask(Id id);
    Id parentId();

}
