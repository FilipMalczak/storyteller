package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.SequentialNodeBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTree;
import com.github.filipmalczak.storyteller.impl.tree.internal.NitriteTreeInternals;
import com.github.filipmalczak.storyteller.impl.tree.internal.TraceEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.SubtaskOrderingStrategy;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.LinkedList;

import static org.valid4j.Assertive.require;
@Flogger
public class SequentialNodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, SequentialNodeBody<Id, Definition, Type, Nitrite>> {

    public SequentialNodeExecution(NitriteTreeInternals<Id, Definition, Type> internals, Definition definition, Type type, SequentialNodeBody<Id, Definition, Type, Nitrite> body, SubtaskOrderingStrategy<Id> orderingStrategy, boolean recordIncorporateToParent) {
        super(internals, definition, type, body, orderingStrategy, recordIncorporateToParent);
    }

    @Override
    protected FluentLogger getLogger() {
        return log;
    }

    @Override
    protected void validateContract() {
        if (type.isRoot()) {
            require(internals.trace().isEmpty(), "Root task needs to be executed without any tasks at the stack");
            require(parent.isEmpty(), "Root task cannot have a parent");
            require(!orderingStrategy.hasExpectations(), "Root task cannot have expected ID");
            require(body instanceof SequentialNodeBody, "Root task body must be implemented as %1", SequentialNodeBody.class.getCanonicalName());
        } else {
            validateSubtaskContract();
            require(!type.isParallel(), "Choice tasks should be executed with chooseNextSteps(...) method");
        }
    }

    private void runInstructions() {
        var storage = internals.storageFactory().read(id);
        var newTrace = new LinkedList<>(internals.trace());
        var newEntry = new TraceEntry<>(thisTask, new LinkedList<>(thisTask.getSubtasks().stream().map(Task::getId).toList()), storage);
        getLogger().atFine().log("Pushing new trace entry: %s", newEntry);
        newTrace.addFirst(newEntry);
        body.perform(
            new NitriteTaskTree<>(
                internals.managers(),
                internals.history(),
                internals.storageFactory().getConfig(),
                internals.idGeneratorFactory(),
                newTrace,
                true
            ),
            storage
        );
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

    @Override
    protected void handleRunning() {
        internals.events().taskPerformed(thisTask, false);
        getLogger().atFine().log("Running instructions of task %s (as always for nodes)", id);
        runInstructions();
    }
}
