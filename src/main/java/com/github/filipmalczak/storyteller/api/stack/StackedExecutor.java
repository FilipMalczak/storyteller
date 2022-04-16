package com.github.filipmalczak.storyteller.api.stack;

import com.github.filipmalczak.storyteller.api.stack.task.*;

public interface StackedExecutor<Id, Definition, Type extends Enum<Type> & TaskType> {
    Task<Id, Definition, Type> executeTask(Definition definition, Type type, TaskBody<Id, Definition, Type> body);

    default Task<Id, Definition, Type> execute(Definition definition, Type type, NodeBody<Id, Definition, Type> body){
        return executeTask(definition, type, body);
    }

    default Task<Id, Definition, Type> execute(Definition definition, Type type, LeafBody body){
        return executeTask(definition, type, body);
    }
//    Task<Id, Definition, Type> execute(Definition definition, Type type, TaskBody<Id, Definition, Type> body, SkippingStrategy strategy);
//
//    default Task<Id, Definition, Type> run(Definition definition, Type type, TaskBody<Id, Definition, Type> body){
//        return execute(definition, type, body, SkippingStrategy.RUN_ALWAYS);
//    }
//
//    default Task<Id, Definition, Type> trySkipping(Definition definition, Type type, TaskBody<Id, Definition, Type> body){
//        return execute(definition, type, body, SkippingStrategy.SKIP_IF_POSSIBLE);
//    }
}
