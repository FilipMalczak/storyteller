package com.github.filipmalczak.storyteller.impl.tree.internal.executor;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;

import java.util.Map;

public interface TaskExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    interface Callback<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
        void beforeRunning(Task<Id, Definition, Type> toRun);
        void onFinished(Task<Id, Definition, Type> finished, Map<Id, HistoryDiff<Id>> increment);
    }

    Task<Id, Definition, Type> executeSequentialNode(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body, Callback<Id, Definition, Type> callback);

    Task<Id, Definition, Type> executeParallelNode(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body, TaskTree.IncorporationFilter<Id, Definition,Type, NoSql> filter, Callback<Id, Definition, Type> callback);

    Task<Id, Definition, Type> executeLeaf(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body, Callback<Id, Definition, Type> callback);

}
