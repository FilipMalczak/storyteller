package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.valid4j.Assertive;

import java.util.LinkedList;

import static org.valid4j.Assertive.require;
@Flogger
class NodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, NodeBody<Id, Definition, Type, Nitrite>> {

    public NodeExecution(NitriteStackedExecutor.NitriteStackedExecutorInternals<Id, Definition, Type> internals, Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        super(internals, definition, type, body);
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
            require(expectations.isEmpty(), "Root task cannot have expected ID");
            require(body instanceof NodeBody, "Root task body must be implemented as %1", NodeBody.class.getCanonicalName());
        } else {
            validateSubtaskContract();
        }
    }

    private void runInstructions() {
        var storage = new NitriteReadStorage(internals.storageConfig(), internals.history(), id);
        var newTrace = new LinkedList<>(internals.trace());
        var newEntry = new TraceEntry<>(thisTask, new LinkedList<>(thisTask.getSubtasks().stream().map(Task::getId).toList()), storage);
        newTrace.addFirst(newEntry);
        body.perform(
            new NitriteStackedExecutor<>(
                internals.managers(),
                internals.history(),
                internals.storageConfig(),
                internals.idGeneratorFactory(),
                newTrace
            ),
            storage
        );
    }

    @Override
    protected void handleRunning() {
        getLogger().atFine().log("Running instructions of task %s (as always for nodes)", id);
        runInstructions();
    }
}
