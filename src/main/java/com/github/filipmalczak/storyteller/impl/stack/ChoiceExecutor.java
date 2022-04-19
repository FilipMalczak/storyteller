package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.storage.InsightIntoNitriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
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
public class ChoiceExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements StackedExecutor<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace;

    @NonNull Map<Id, HistoryTracker<Id>> histories = new HashMap<>();

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        var subtaskHistory = history.copy();
        var subtask = new NitriteStackedExecutor<>(managers, subtaskHistory, storageConfig, idGeneratorFactory, new ArrayList<>(trace)).execute(definition, type, body);
        histories.put(subtask.getId(), subtaskHistory);
        return subtask;
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        var subtaskHistory = history.copy();
        var subtask = new NitriteStackedExecutor<>(managers, subtaskHistory, storageConfig, idGeneratorFactory, new ArrayList<>(trace)).execute(definition, type, body);
        histories.put(subtask.getId(), subtaskHistory);
        return subtask;
    }

    @Override
    public Task<Id, Definition, Type> chooseNextSteps(Definition definition, Type type, ChoiceBody<Id, Definition, Type, Nitrite> body) {
        var subtaskHistory = history.copy();
        var subtask = new NitriteStackedExecutor<>(managers, subtaskHistory, storageConfig, idGeneratorFactory, new ArrayList<>(trace)).chooseNextSteps(definition, type, body);
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
