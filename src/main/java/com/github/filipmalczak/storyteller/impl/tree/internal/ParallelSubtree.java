package com.github.filipmalczak.storyteller.impl.tree.internal;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.ParallelNodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.SequentialNodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Insight;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.InsightIntoNitriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTree;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.Events;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.valid4j.Assertive.require;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class ParallelSubtree<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskTree<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory;
    @NonNull Events<Id> events;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace;
    @NonNull Map<Id, Long> startingTimestamps = new HashMap<>();
    @NonNull Map<Id, IncrementalHistoryTracker<Id>> histories = new HashMap<>();

    private Task<Id, Definition, Type> branchOff(Function<TaskTree<Id, Definition, Type, Nitrite>, Task<Id, Definition, Type>> specification){
        var timestamp = System.currentTimeMillis();
        var subtaskHistory = history.snapshot();
        var delegateTree = new NitriteTaskTree<>(
            managers,
            subtaskHistory,
            storageConfig,
            idGeneratorFactory,
            new ArrayList<>(trace),
            events,
            false
        );
        var subtask = specification.apply(delegateTree);
        histories.put(subtask.getId(), subtaskHistory);
        startingTimestamps.put(subtask.getId(), timestamp);
        return subtask;
    }

    @Override
    public Task<Id, Definition, Type> executeSequential(Definition definition, Type type, SequentialNodeBody<Id, Definition, Type, Nitrite> body) {
        return branchOff(tree -> tree.executeSequential(definition, type, body));
    }

    @Override
    public Task<Id, Definition, Type> executeParallel(Definition definition, Type type, ParallelNodeBody<Id, Definition, Type, Nitrite> body) {
        return branchOff(tree -> tree.executeParallel(definition, type, body));
    }

    @Override
    public Task<Id, Definition, Type> executeSequential(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        return branchOff(tree -> tree.executeSequential(definition, type, body));
    }

    public IncrementalHistoryTracker<Id> getHistory(Task<Id, Definition, Type> task){
        return getHistory(task.getId());
    }

    public IncrementalHistoryTracker<Id> getHistory(Id id){
        return histories.get(id);
    }

    public long getStartTimestamp(Task<Id, Definition, Type> task){
        return getStartTimestamp(task.getId());
    }

    public long getStartTimestamp(Id id){
        return startingTimestamps.get(id);
    }


    public Insight<Id, Definition, Type, Nitrite> getInsights() {
        return id -> {
            log.atFine().log("Insight into %s", id);
            require(histories.containsKey(id));
            return new InsightIntoNitriteStorage<>(storageConfig, histories.get(id), histories.get(id).getWritingAncestors(id).findFirst().get());
        };
    }
}
