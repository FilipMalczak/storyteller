package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadWriteStorage;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import static org.valid4j.Assertive.require;

@Flogger
class LeafExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, LeafBody<Id, Definition, Type, Nitrite>> {

    public LeafExecution(NitriteStackedExecutor.NitriteStackedExecutorInternals<Id, Definition, Type> internals, Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> idDefinitionTypeNitriteLeafBody, SubtaskOrderingStrategy<Id> orderingStrategy, boolean recordIncorporateToParent) {
        super(internals, definition, type, idDefinitionTypeNitriteLeafBody, orderingStrategy, recordIncorporateToParent);
    }

    @Override
    protected FluentLogger getLogger() {
        return log;
    }

    @Override
    protected void validateContract() {
        validateSubtaskContract();
        require(!type.isChoice(), "Choice tasks should be executed with chooseNextSteps(...) method");
    }

    @Override
    protected void handleRunning() {
        internals.events().taskPerformed(thisTask, finished);
        if (!finished) {
            getLogger().atFine().log("Running instructions of unfinished leaf task %s", id);
            runInstructions();
        } else {
            getLogger().atFine().log("Skipping already finished subtask %s", id);
        }
    }

    private void runInstructions() {
        var storage = new NitriteReadWriteStorage(internals.storageConfig(), internals.history(), id);
        try {
            body.perform(storage);
            storage.flush();
        } catch (Exception e) {
            storage.purge();
            throw e;
        }
    }
}
