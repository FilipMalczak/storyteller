package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.*;

import static org.hamcrest.CoreMatchers.not;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@EqualsAndHashCode
@Flogger
@AllArgsConstructor
public class NitriteStackedExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    implements StackedExecutor<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace; // trace[0] - parent; trace[-1] - root; empty for root
    JournalEntryFactory journalEntryFactory;

    public static record NitriteStackedExecutorInternals<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> (
        NitriteManagers<Id, Definition, Type> managers,
        HistoryTracker<Id> history,
        NitriteStorageConfig<Id> storageConfig,
        IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory,
        List<TraceEntry<Id, Definition, Type>> trace,
        JournalEntryFactory journalEntryFactory
    ) {}

    private NitriteStackedExecutorInternals<Id, Definition, Type> getInternals(){
        return new NitriteStackedExecutorInternals<Id, Definition, Type>(
            managers,
            history,
            storageConfig,
            idGeneratorFactory,
            trace,
            journalEntryFactory
        );
    }

    public NitriteStackedExecutor(@NonNull NitriteManagers<Id, Definition, Type> managers, @NonNull HistoryTracker<Id> history, @NonNull NitriteStorageConfig<Id> storageConfig, @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory, @NonNull List<TraceEntry<Id, Definition, Type>> trace) {
        this.managers = managers;
        this.history = history;
        this.storageConfig = storageConfig;
        this.idGeneratorFactory = idGeneratorFactory;
        this.trace = trace;
        this.journalEntryFactory = new JournalEntryFactory(managers.getSessionManager());
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        var strategy = new LinearSubtaskOrderingStrategy<Id, Definition, Type>(
            trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds).orElseGet(LinkedList::new)
        );
        var execution = new NodeExecution(getInternals(), definition, type, body, strategy);
        strategy.setFriend(execution.getFriend());
        execution.run();
        return execution.getThisTask();
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        var strategy = new LinearSubtaskOrderingStrategy<Id, Definition, Type>(
            trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds).orElseGet(LinkedList::new)
        );
        var execution = new LeafExecution(getInternals(), definition, type, body, strategy);
        strategy.setFriend(execution.getFriend());
        execution.run();
        return execution.getThisTask();
    }

    @Override
    public Task<Id, Definition, Type> chooseNextSteps(Definition definition, Type type, ChoiceBody<Id, Definition, Type, Nitrite> body) {
        var strategy = new AnyOrderStrategy<Id, Definition, Type>(
            trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds).orElseGet(LinkedList::new)
        );
        var execution = new ChoiceExecution<>(getInternals(), definition, type, body, strategy);
        strategy.setFriend(execution.getFriend());
        execution.run();
        return execution.getThisTask();
    }

}
