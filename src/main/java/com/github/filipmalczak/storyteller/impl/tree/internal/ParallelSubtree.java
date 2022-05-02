package com.github.filipmalczak.storyteller.impl.tree.internal;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.ParallelNodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Insight;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageFactory;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTree;
import com.github.filipmalczak.storyteller.impl.tree.config.MergeSpecFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
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
    @NonNull MergeSpecFactory<Id, Definition, Type> mergeSpecFactory;
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
            mergeSpecFactory,
            new ArrayList<>(trace),
            false
        );
        var subtask = specification.apply(delegateTree);
        histories.put(subtask.getId(), subtaskHistory);
        startingTimestamps.put(subtask.getId(), timestamp);
        return subtask;
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        return branchOff(tree -> tree.execute(definition, type, body));
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body, IncorporationFilter<Id, Definition, Type, Nitrite> filter) {
        return branchOff(tree -> tree.execute(definition, type, body, filter));
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        return branchOff(tree -> tree.execute(definition, type, body));
    }

    public IncrementalHistoryTracker<Id> getHistory(Id id){
        return histories.get(id);
    }

    public long getStartTimestamp(Id id){
        return startingTimestamps.get(id);
    }

    public Insight<Id, Nitrite> getInsights() {
        return id -> {
            log.atFine().log("Obtaining insight into %s", id);
            require(histories.containsKey(id)); //todo specialized exception
            return new NitriteStorageFactory<>(
                    managers.getNitrite(),
                    storageConfig,
                    histories.get(id)
                )
                .insight(
                    histories
                        .get(id)
                        .getWritingAncestors(id)
                        .findFirst()
                        .get()
                );
        };
    }
}
