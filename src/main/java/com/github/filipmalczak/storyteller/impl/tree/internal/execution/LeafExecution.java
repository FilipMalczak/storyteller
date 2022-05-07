package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import lombok.Value;

@Value
public class LeafExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> implements Execution<Id, Definition, Type> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> executionContext;
    LeafBody<Id, Definition, Type, NoSql> body;

    @Override
    public ExecutionContext<Id, Definition, Type> run() {
        if (!executionContext.isStarted()) {
            executionContext.events().taskStarted();
        }
        if (executionContext.isFinished()){
            executionContext.events().taskPerformed(true);
        } else {
            body.perform(null); // todo
            executionContext.events().taskPerformed(false);
            executionContext.events().taskEnded();
        }
        return executionContext;
    }
}
