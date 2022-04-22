package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.ChoiceBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadWriteStorage;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

@Flogger
class ChoiceExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, ChoiceBody<Id, Definition, Type, Nitrite>> {


    public ChoiceExecution(NitriteStackedExecutor.NitriteStackedExecutorInternals<Id, Definition, Type> internals, Definition definition, Type type, ChoiceBody<Id, Definition, Type, Nitrite> idDefinitionTypeNitriteChoiceBody, SubtaskOrderingStrategy<Id> orderingStrategy, boolean recordIncorporateToParent) {
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
        var choiceExecutor = new ChoiceExecutor<>(
            internals.managers(),
            internals.history(),
            internals.storageConfig(),
            internals.idGeneratorFactory(),
            internals.events(),
            newTrace
        );
        var chosen = body.makeChoice(choiceExecutor, storage, choiceExecutor.getInsights());
        internals.events().decided(thisTask, chosen);
        acceptHistory(choiceExecutor.getHistory(chosen));
    }

    private void acceptHistory(HistoryTracker<Id> toAccept){
        internals.history().mirror(toAccept);
    }
}
