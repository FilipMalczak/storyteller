package com.github.filipmalczak.storyteller.impl.tree.internal.executor;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import lombok.Value;
import org.dizitart.no2.Nitrite;

@Value
public class SubtaskAdapter<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements TaskTree<Id, Definition, Type, Nitrite> {
    TaskExecutor<Id, Definition, Type, Nitrite> executor;
    TaskExecutor.Callback<Id, Definition, Type> callback;

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
        return executor.executeSequentialNode(definition, type, body, callback);
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body, IncorporationFilter<Id, Definition, Type, Nitrite> filter) {
        return executor.executeParallelNode(definition, type, body, filter, callback);
    }

    @Override
    public Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
        return executor.executeLeaf(definition, type, body, callback);
    }
}
