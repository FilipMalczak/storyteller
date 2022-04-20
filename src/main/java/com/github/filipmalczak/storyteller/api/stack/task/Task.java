package com.github.filipmalczak.storyteller.api.stack.task;

import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.SubtaskDefined;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.SubtaskDisowned;
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

    public Stream<Task<Id, Definition, Type>> getDisownedSubtasks(){
        return getJournalEntries()
            .filter(e -> e instanceof SubtaskDisowned)
            .map(e -> ((SubtaskDisowned) e).getDisownedSubtask());
    }

    @Override
    public String toString() {
        return "Task(" +
            "id=" + id +
            ", definition=" + definition +
            ", type=" + type +
            ", subtasks=" + subtasks +
            ", journal[" + journal.size()+"]" +
            ')';
    }
}
