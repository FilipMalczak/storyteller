package com.github.filipmalczak.storyteller.stack.task;

import com.github.filipmalczak.storyteller.stack.task.journal.entries.JournalEntry;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Value
@Builder
public class Task<Id, Definition, Type extends Enum<Type> & TaskType> {
    @NonNull Id id;
    @NonNull Definition definition;
    @NonNull Type type;
    @Builder.Default
    List<Task<Id, Definition, Type>>  subtasks = new LinkedList<>();
    @Builder.Default
    List<JournalEntry> journal = new LinkedList<>();

    public JournalEntry record(JournalEntry entry){
        journal.add(entry);
        return entry;
    }

    public Stream<JournalEntry> getJournalEntries() {
        return journal.stream();
    }
}
