package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.tree.Sessions;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.NitriteTreeInternals;
import com.github.filipmalczak.storyteller.impl.tree.internal.TraceEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.ChoiceExecution;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.LeafExecution;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.NodeExecution;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.Events;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.AnyOrderStrategy;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.LinearSubtaskOrderingStrategy;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
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
public class NitriteTaskTree<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    implements TaskTreeRoot<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace; // trace[0] - parent; trace[-1] - root; empty for root
    @NonNull Events<Id> events;
    boolean recordIncorporateToParent;

    private NitriteTreeInternals<Id, Definition, Type> getInternals(){
        return new NitriteTreeInternals<Id, Definition, Type>(
            managers,
            history,
            storageConfig,
            idGeneratorFactory,
            trace,
            events
        );
    }

    @Override
    public Task<Id, Definition, Type> executeOrdered(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        var strategy = new LinearSubtaskOrderingStrategy<Id, Definition, Type>(
            trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds).orElseGet(LinkedList::new)
        );
        var execution = new NodeExecution(getInternals(), definition, type, body, strategy, recordIncorporateToParent);
        strategy.setFriend(execution.getFriend());
        execution.run();
        return execution.getThisTask();
    }

    @Override
    public Task<Id, Definition, Type> executeOrdered(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        var strategy = new LinearSubtaskOrderingStrategy<Id, Definition, Type>(
            trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds).orElseGet(LinkedList::new)
        );
        var execution = new LeafExecution(getInternals(), definition, type, body, strategy, recordIncorporateToParent);
        strategy.setFriend(execution.getFriend());
        execution.run();
        return execution.getThisTask();
    }

    @Override
    public Task<Id, Definition, Type> chooseBranch(Definition definition, Type type, ChoiceBody<Id, Definition, Type, Nitrite> body) {
        var strategy = new AnyOrderStrategy<Id, Definition, Type>(
            trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds).orElseGet(LinkedList::new)
        );
        var execution = new ChoiceExecution<>(getInternals(), definition, type, body, strategy, recordIncorporateToParent);
        strategy.setFriend(execution.getFriend());
        execution.run();
        return execution.getThisTask();
    }

    @Override
    public Sessions getSessions() {
        return managers.getSessionManager();
    }
}
