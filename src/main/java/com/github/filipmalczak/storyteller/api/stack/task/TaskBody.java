package com.github.filipmalczak.storyteller.api.stack.task;

public sealed interface TaskBody<Id, Definition, Type extends Enum<Type> & TaskType> permits LeafBody, NodeBody {//<Id, Definition, Type extends Enum<Type> & TaskType> {
//    void perform(StackedExecutor<Id, Definition, Type> executor, Storage storage);
}
