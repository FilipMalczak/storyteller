package com.github.filipmalczak.storyteller.impl.tree.internal;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.Events;

import java.util.List;

/**
 * @see ExecutionFriend
 */
public record NitriteTreeInternals<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>(
    NitriteManagers<Id, Definition, Type> managers,
    NitriteStorageFactory<Id> storageFactory,
    HistoryTracker<Id> history,
    IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory,
    List<TraceEntry<Id, Definition, Type>> trace,
    Events<Id> events
) {
}
