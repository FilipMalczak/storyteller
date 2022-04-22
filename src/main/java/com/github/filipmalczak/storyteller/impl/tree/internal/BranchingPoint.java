package com.github.filipmalczak.storyteller.impl.tree.internal;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTree;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.Events;
import com.github.filipmalczak.storyteller.impl.storage.InsightIntoNitriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.*;

import static org.valid4j.Assertive.require;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class BranchingPoint<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskTree<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory;
    @NonNull Events<Id> events;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace;

    @NonNull Map<Id, HistoryTracker<Id>> histories = new HashMap<>();

    @Override
    public Task<Id, Definition, Type> executeOrdered(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        var subtaskHistory = history.copy();
        var subtask = new NitriteTaskTree<>(
            managers,
            subtaskHistory,
            storageConfig,
            idGeneratorFactory,
            new ArrayList<>(trace),
            events,
            false
        ).executeOrdered(definition, type, body);
        histories.put(subtask.getId(), subtaskHistory);
        return subtask;
    }

    @Override
    public Task<Id, Definition, Type> executeOrdered(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        var subtaskHistory = history.copy();
        var subtask = new NitriteTaskTree<>(
            managers,
            subtaskHistory,
            storageConfig,
            idGeneratorFactory,
            new ArrayList<>(trace),
            events,
            false
        ).executeOrdered(definition, type, body);
        histories.put(subtask.getId(), subtaskHistory);
        return subtask;
    }

    @Override
    public Task<Id, Definition, Type> chooseBranch(Definition definition, Type type, ChoiceBody<Id, Definition, Type, Nitrite> body) {
        var subtaskHistory = history.copy();
        var subtask = new NitriteTaskTree<>(
            managers,
            subtaskHistory,
            storageConfig,
            idGeneratorFactory,
            new ArrayList<>(trace),
            events,
            false
        ).chooseBranch(definition, type, body);
        histories.put(subtask.getId(), subtaskHistory);
        return subtask;
    }

    public HistoryTracker<Id> getHistory(Task<Id, Definition, Type> task){
        return getHistory(task.getId());
    }

    public HistoryTracker<Id> getHistory(Id id){
        return histories.get(id);
    }

    public ChoiceBody.Insight<Id, Definition, Type, Nitrite> getInsights() {
        return id -> {
            log.atFine().log("Insight into %s", id);
            require(histories.containsKey(id));
            return new InsightIntoNitriteStorage<>(storageConfig, histories.get(id), histories.get(id).getLeaves(id).findFirst().get());
        };
    }
}
