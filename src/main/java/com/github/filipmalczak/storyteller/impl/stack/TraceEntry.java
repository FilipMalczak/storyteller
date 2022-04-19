package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TraceEntry<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    private Task<Id, Definition, Type> executedTask;
    private final List<Id> expectedSubtaskIds;
    private NitriteReadStorage<Id> storage;
}
