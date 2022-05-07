package com.github.filipmalczak.storyteller.impl.tree.internal.executor;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.ExecutionFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.ExecutionFactoryImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import lombok.Value;
import org.dizitart.no2.Nitrite;

@Value
public class TaskExecutorImpl<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskExecutor<Id, Definition, Type, Nitrite> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> context;

    @Override
    public Task<Id, Definition, Type> executeSequentialNode(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body, Callback<Id, Definition, Type> callback) {
        var context = new ExecutionFactoryImpl<>(treeContext)
            .inScopeOf(this.context)
            .sequentialNode(definition, type, body)
            .run();
        callback.onFinished(context.task(), context.history().getIncrement());
        return context.task();
    }

    @Override
    public Task<Id, Definition, Type> executeParallelNode(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body, TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter, Callback<Id, Definition, Type> callback) {
        var context = new ExecutionFactoryImpl<>(treeContext)
            .inScopeOf(this.context)
            .parallelNode(definition, type, body, filter)
            .run();
        callback.onFinished(context.task(), context.history().getIncrement());
        return context.task();
    }

    @Override
    public Task<Id, Definition, Type> executeLeaf(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body, Callback<Id, Definition, Type> callback) {
        var context = new ExecutionFactoryImpl<>(treeContext)
            .inScopeOf(this.context)
            .leaf(definition, type, body)
            .run();
        callback.onFinished(context.task(), context.history().getIncrement());
        return context.task();
    }
}
