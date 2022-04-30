package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ParallelNodeBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteParallelStorage;
import com.github.filipmalczak.storyteller.impl.tree.internal.NitriteTreeInternals;
import com.github.filipmalczak.storyteller.impl.tree.internal.ParallelSubtree;
import com.github.filipmalczak.storyteller.impl.tree.internal.TraceEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.SubtaskOrderingStrategy;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.LinkedList;

@Flogger
public class ParallelNodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, ParallelNodeBody<Id, Definition, Type, Nitrite>> {


    public ParallelNodeExecution(NitriteTreeInternals<Id, Definition, Type> internals, Definition definition, Type type, ParallelNodeBody<Id, Definition, Type, Nitrite> body, SubtaskOrderingStrategy<Id> orderingStrategy, boolean recordIncorporateToParent) {
        super(internals, definition, type, body, orderingStrategy, recordIncorporateToParent);
    }

    @Override
    protected FluentLogger getLogger() {
        return log;
    }

    @Override
    protected void validateContract() {
        validateSubtaskContract();
    }

    @Override
    protected void handleRunning() {
        internals.events().taskPerformed(thisTask, false);
        getLogger().atFine().log("Running instructions of task %s (as always for nodes)", id);
        runInstructions();

    }

    private void runInstructions() {
        var storage = internals.storageFactory().parallelRead(id);
        var newTrace = new LinkedList<>(internals.trace());
        var newEntry = new TraceEntry<>(thisTask, new LinkedList<>(thisTask.getSubtasks().stream().map(Task::getId).toList()), storage);
        getLogger().atFine().log("Pushing new trace entry: %s", newEntry);
        newTrace.addFirst(newEntry);

        var subtree = new ParallelSubtree<>(
            internals.managers(),
            internals.history(),
            internals.storageFactory().getConfig(),
            internals.idGeneratorFactory(),
            newTrace
        );
        body.perform(subtree, storage, subtree.getInsights(), t -> {
            internals.history().apply(subtree.getHistory(t).getIncrement());
            storage.incorporate(subtree.getStartTimestamp(t), subtree.getInsights().into(t).documents());
            internals.events().subtaskIncorporated(thisTask, t);
        });
        storage.flush();
        if (!newEntry.getExpectedSubtaskIds().isEmpty()) {
            log.atFine().log("After running the node some subtasks are still expected; disowning them, as the node has narrowed");
            internals.events().bodyShrunk(
                thisTask,
                newEntry
                    .getExpectedSubtaskIds()
                    .stream()
                    .map(
                        internals
                            .managers()
                            .getTaskManager()::getById
                    )
                    .map(t -> (Task) t)
                    .toList()
            );
            disownExpectedUpTheTrace(newTrace);
        }
    }

}
