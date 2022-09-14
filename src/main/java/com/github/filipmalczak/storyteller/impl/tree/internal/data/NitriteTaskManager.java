package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.TaskData;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization.TaskSerializer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.Optional;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PACKAGE)
@Flogger
public class NitriteTaskManager<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskManager<Id, Definition, Type> {
    @NonNull ObjectRepository<TaskData> repository;
    @NonNull TaskSerializer<Id, Definition, Type> serializer;
    @NonNull EventsPersistence<Id> eventsPersistence;

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

    /**
     * Does not persist recorded journal entries. OTOH, journal entry manager adds (in-memory) recorded entries to
     * the task and persists them in a way that will be reflected in future runtimes.
     */
    @Override
    public void update(Task<Id, Definition, Type> task) {
        log.atFine().log("Updating task %s", task);
        repository.update(serializer.fromTask(task));
    }
}
