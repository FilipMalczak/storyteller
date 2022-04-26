package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.github.filipmalczak.storyteller.impl.tree.internal.BranchingPoint;
import com.github.filipmalczak.storyteller.impl.tree.internal.NitriteTreeInternals;
import com.github.filipmalczak.storyteller.impl.tree.internal.TraceEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.SubtaskOrderingStrategy;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.LinkedList;

@Flogger
public class ChoiceExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, ChoiceBody<Id, Definition, Type, Nitrite>> {


    public ChoiceExecution(NitriteTreeInternals<Id, Definition, Type> internals, Definition definition, Type type, ChoiceBody<Id, Definition, Type, Nitrite> idDefinitionTypeNitriteChoiceBody, SubtaskOrderingStrategy<Id> orderingStrategy, boolean recordIncorporateToParent) {
        super(internals, definition, type, idDefinitionTypeNitriteChoiceBody, orderingStrategy, recordIncorporateToParent);
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
        getLogger().atFine().log("Running instructions of task %s (as always for choices)", id);
        runInstructions();
    }

    private void runInstructions() {
        //todo copypaste from NodeExecution
        var storage = new NitriteReadStorage<>(internals.storageConfig(), internals.history(), id);
        var newTrace = new LinkedList<>(internals.trace());
        var newEntry = new TraceEntry<>(thisTask, new LinkedList<>(thisTask.getSubtasks().stream().map(Task::getId).toList()), storage);
        getLogger().atFine().log("Pushing new trace entry: %s", newEntry);
        newTrace.addFirst(newEntry);

        var choiceExecutor = new BranchingPoint<>(
            internals.managers(),
            internals.history(),
            internals.storageConfig(),
            internals.idGeneratorFactory(),
            internals.events(),
            newTrace
        );
        var chosen = body.makeChoice(choiceExecutor, storage, choiceExecutor.getInsights());
        internals.events().decided(thisTask, chosen);
        internals.history().apply(choiceExecutor.getHistory(chosen).getIncrement());
    }

}
