package com.github.filipmalczak.storyteller.api.tree.task;

import com.github.filipmalczak.storyteller.api.tree.TaskResolver;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.SubtaskDefined;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.SubtaskDisowned;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;

@Value
@Builder
public class SimpleTask<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements Task<Id, Definition, Type> {
    @NonNull Id id;
    @NonNull Definition definition;
    @NonNull Type type;
    Id parentId;
    Id previousSiblingId;
    @NonNull TaskResolver<Id, Definition, Type> taskResolver;
    @Builder.Default
    List<JournalEntry> journal = new LinkedList<>();

    public JournalEntry record(JournalEntry entry){
        journal.add(entry);
        return entry;
    }

    public Stream<JournalEntry> getJournalEntries() {
        return journal.stream();
    }

    @Override
    public Stream<Id> getSubtaskIds() {
        var disownedIds = getDisownedSubtaskIds().toList();
        return getJournalEntries()
            .filter(e -> e instanceof SubtaskDefined)
            .map(e -> ((SubtaskDefined<Id>) e).getDefinedSubtaskId())
            .filter(not(disownedIds::contains));
    }

    @Override
    public Optional<Task<Id, Definition, Type>> findSubtask(Id id) {
        if (getSubtaskIds().anyMatch(isEqual(id)))
            return taskResolver.resolve(id);
        return Optional.empty();
    }

    @Override
    public Stream<Task<Id, Definition, Type>> getSubtasks() {
        return getSubtaskIds().map(taskResolver::resolve).map(Optional::get);
    }

    @Override
    public Stream<Id> getDisownedSubtaskIds() {
        return getJournalEntries()
            .filter(e -> e instanceof SubtaskDisowned)
            .map(e -> ((SubtaskDisowned<Id>) e).getDisownedSubtaskId());
    }

    @Override
    public Optional<Task<Id, Definition, Type>> findDisownedSubtask(Id id) {
        if (getDisownedSubtaskIds().anyMatch(isEqual(id)))
            return taskResolver.resolve(id);
        return Optional.empty();
    }

    @Override
    public Stream<Task<Id, Definition, Type>> getDisownedSubtasks() {
        return getDisownedSubtaskIds().map(taskResolver::resolve).map(Optional::get);
    }


    @Override
    public String toString() {
        return "Task(" +
            "id=" + id +
            ", definition=" + definition +
            ", type=" + type +
            ", subtasks=" + getSubtaskIds().count() +
            ", disowned=" + getDisownedSubtaskIds().count() +
            ", journal[" + journal.size()+"]" +
            ')';
    }
}
