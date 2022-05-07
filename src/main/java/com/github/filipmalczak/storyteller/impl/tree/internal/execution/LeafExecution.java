package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageFactory;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import lombok.Value;
import org.dizitart.no2.Nitrite;

@Value
public class LeafExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements Execution<Id, Definition, Type> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> executionContext;
    LeafBody<Id, Definition, Type, Nitrite> body;

    @Override
    public ExecutionContext<Id, Definition, Type> context() {
        return executionContext;
    }

    @Override
    public void run() {
        if (executionContext.isFinished()){
            executionContext.events().taskPerformed(true);
        } else {
            var storage = new NitriteStorageFactory<>(
                    treeContext.getNitriteManagers().getNitrite(),
                    treeContext.getStorageConfig(),
                    executionContext.history()
                )
                .readWrite(executionContext.id());
            body.perform(storage);
            storage.flush();
            executionContext.events().taskPerformed(false);
        }
    }
}
