package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.impl.stack.data.model.TaskData;
import com.github.filipmalczak.storyteller.impl.stack.data.serialization.TaskSerializer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.Optional;

import static java.util.function.Predicate.not;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PACKAGE)
@Flogger
public class NitriteTaskManager<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskManager<Id, Definition, Type> {
    @NonNull ObjectRepository<TaskData> repository;
    @NonNull TaskSerializer<Id, Definition, Type> serializer;
    @NonNull JournalEntryManager<Id> journalEntryManager;

    @Override
    public Optional<Task<Id, Definition, Type>> findById(Id id) {
        var found = repository.find(eq("id", id)).toList();
        require(found.size() < 2, "Task ID needs to be unique");
        if (found.isEmpty())
            return Optional.empty();
        return Optional.of(serializer.toTask(found.get(0)));
    }

    @Override
    public void register(Task<Id, Definition, Type> task) {
        log.atFine().log("Registering task %s", task);
        require(findById(task.getId()).isEmpty(), "Task can only be registered once!");
        repository.insert(serializer.fromTask(task));
    }

    @Override
    public void update(Task<Id, Definition, Type> task) {
        log.atFine().log("Updating task %s", task);
        //todo wrap in a transaction
        repository.update(serializer.fromTask(task));
        var existingEntries = journalEntryManager.findByTask(task).toList();
        //todo I forgot that task manager persists journal entries too; they get persisted by executions too
        task
            .getJournalEntries()
            .filter(not(existingEntries::contains))
            .forEach(e -> journalEntryManager.record(task, e));
    }
}
