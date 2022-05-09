package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageFactory;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.SubtaskAdapter;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.TaskExecutor;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.TaskExecutorImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import lombok.Value;
import org.dizitart.no2.Nitrite;

import java.util.Map;

@Value
public class SequentialNodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements Execution<Id, Definition, Type> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> executionContext;
    NodeBody<Id, Definition, Type, Nitrite> body;

    @Override
    public ExecutionContext<Id, Definition, Type> context() {
        return executionContext;
    }

    @Override
    public void run() {
        var storage = new NitriteStorageFactory<>(
                treeContext.getNitriteManagers().getNitrite(),
                treeContext.getStorageConfig(),
                executionContext.history()
            )
            .read(executionContext.id());
        var tree = new SubtaskAdapter<>(
            new TaskExecutorImpl<>(treeContext, executionContext),
            new TaskExecutor.Callback<Id, Definition, Type>() {
                @Override
                public void beforeRunning(Task<Id, Definition, Type> finished) {

                }

                @Override
                public void onFinished(Task<Id, Definition, Type> finished, Map<Id, HistoryDiff<Id>> increment) {
                    executionContext.incorporate(finished.getId(), increment, finished.getType().isWriting());
                    storage.reload();
                }
            }
        );
        body.perform(tree, storage);
        if (!executionContext.expectations().isEmpty()) {
            executionContext.events().bodyNarrowed(executionContext.expectations());
            executionContext.disownExpectations();
        }
        executionContext.events().taskPerformed(false);
        if (executionContext.needsAmendment()) {
            executionContext.events().taskAmended();
        }
    }
}
