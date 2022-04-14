package com.github.filipmalczak.storyteller.stack;

import com.github.filipmalczak.storyteller.stack.task.Task;
import com.github.filipmalczak.storyteller.stack.task.TaskBody;
import com.github.filipmalczak.storyteller.stack.task.TaskType;

public interface StackedExecutor<Id, Definition, Type extends Enum<Type> & TaskType> {
    Task<Id, Definition, Type> execute(Definition definition, Type type, TaskBody<Id, Definition, Type> body);
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
