package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.NullContext;

public sealed interface ExecutionFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> permits ExecutionFactoryImpl {
    interface Scoped<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
        //todo this will change when we introduce TaskSpec
        Execution<Id, Definition, Type> sequentialNode(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body, boolean userDefinedTask);
        Execution<Id, Definition, Type> parallelNode(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body, TaskTree.IncorporationFilter<Id, Definition,Type, NoSql> filter, boolean userDefinedTask);
        Execution<Id, Definition, Type> leaf(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body, boolean userDefinedTask);

        default Execution<Id, Definition, Type> sequentialNode(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body) {
            return sequentialNode(definition, type, body, true);
        }

        default Execution<Id, Definition, Type> parallelNode(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body, TaskTree.IncorporationFilter<Id, Definition,Type, NoSql> filter) {
            return parallelNode(definition, type, body, filter, true);
        }

        default Execution<Id, Definition, Type> leaf(Definition definition, Type type, LeafBody<Id, Definition, Type, NoSql> body) {
            return leaf(definition, type, body, true);
        }
    }

    default Execution<Id, Definition, Type> rootExecution(Definition definition, Type type, NodeBody<Id, Definition, Type, NoSql> body){
        return inScopeOf(new NullContext<>()).sequentialNode(definition, type, body);
    }

    Scoped<Id, Definition, Type, NoSql> inScopeOf(ExecutionContext<Id, Definition, Type> context);
}
