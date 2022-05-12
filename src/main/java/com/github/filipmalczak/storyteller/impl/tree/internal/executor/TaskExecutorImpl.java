package com.github.filipmalczak.storyteller.impl.tree.internal.executor;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.ThrowingAlreadyRecordedException;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.Execution;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.ExecutionFactoryImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

@Value
@Flogger
@AllArgsConstructor
public class TaskExecutorImpl<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskExecutor<Id, Definition, Type, Nitrite> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> context;
    boolean forUserDefinedTasks;

    public TaskExecutorImpl(TreeContext<Id, Definition, Type> treeContext, ExecutionContext<Id, Definition, Type> context) {
        this(treeContext, context, true);
    }

    @SneakyThrows
    private Task<Id, Definition, Type> performLifecycle(Execution<Id, Definition, Type> execution, Callback<Id, Definition, Type> callback){
        var ctx = execution.context();
        if (!ctx.isRoot()){
            log.atFine().log("Task %s is root, starting history from parent: %s", ctx.id(), ctx.parent().id());
            ctx.history().startFrom(ctx.id(), ctx.parent().id());
            log.atFine().log("All ancestors: %s", ctx.history().getAllAncestors(ctx.id()).toList());
            log.atFine().log("Writing ancestors: %s", ctx.history().getWritingAncestors(ctx.id()).toList());
        } else {
            log.atFine().log("Task %s is root, starting history from scratch", ctx.id());
        }
        if (!ctx.isStarted()){
            ctx.events().taskStarted();
        }
        callback.beforeRunning(ctx.task());
        try {
            execution.run();
            ctx.makeHistory();
            if (!ctx.isFinished()){
                ctx.events().taskEnded();
            }
            callback.onFinished(ctx.task(), ctx.history().getIncrement());
        } catch (Exception e){
            if (e instanceof ThrowingAlreadyRecordedException) {
                ctx.events().taskInterrupted();
                if (ctx.isRoot()) {
                    throw ((ThrowingAlreadyRecordedException) e).getAlreadyRecorded();
                } else {
                    throw e;
                }
            } else {
                ctx.events().exeptionCaught(e);
                if (ctx.isRoot()) {
                    throw e;
                } else {
                    throw new ThrowingAlreadyRecordedException(e);
                }
            }
        }
        return ctx.task();
    }

    @Override
    public Task<Id, Definition, Type> executeSequentialNode(TaskSpec<Definition, Type> taskSpec, NodeBody<Id, Definition, Type, Nitrite> body, Callback<Id, Definition, Type> callback) {
        return performLifecycle(
            new ExecutionFactoryImpl<>(treeContext)
                .inScopeOf(this.context)
                .sequentialNode(taskSpec, body, forUserDefinedTasks),
            callback
        );
    }

    @Override
    public Task<Id, Definition, Type> executeParallelNode(TaskSpec<Definition, Type> taskSpec, NodeBody<Id, Definition, Type, Nitrite> body, TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter, Callback<Id, Definition, Type> callback) {
        return performLifecycle(
            new ExecutionFactoryImpl<>(treeContext)
                .inScopeOf(this.context)
                .parallelNode(taskSpec, body, filter, forUserDefinedTasks),
            callback
        );
    }

    @Override
    public Task<Id, Definition, Type> executeLeaf(TaskSpec<Definition, Type> taskSpec, LeafBody<Id, Definition, Type, Nitrite> body, Callback<Id, Definition, Type> callback) {
        return performLifecycle(
            new ExecutionFactoryImpl<>(treeContext)
                .inScopeOf(this.context)
                .leaf(taskSpec, body, forUserDefinedTasks),
            callback
        );
    }
}
