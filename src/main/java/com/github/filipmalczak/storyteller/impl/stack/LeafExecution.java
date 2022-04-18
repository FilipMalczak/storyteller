package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadWriteStorage;
import com.google.common.flogger.FluentLogger;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

@Flogger
class LeafExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, LeafBody<Id, Definition, Type, Nitrite>> {

    public LeafExecution(NitriteStackedExecutor.NitriteStackedExecutorInternals<Id, Definition, Type> internals, Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        super(internals, definition, type, body);
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
        if (!finished) {
            getLogger().atFine().log("Running instructions of unfinished leaf task %s", id);
            record(internals.journalEntryFactory().instructionsRan());
            runInstructions();
        } else {
            getLogger().atFine().log("Skipping already finished subtask %s", id);
            record(internals.journalEntryFactory().instructionsSkipped());
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
