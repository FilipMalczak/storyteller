package com.github.filipmalczak.storyteller.stack.task;

import com.github.filipmalczak.storyteller.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.storage.Storage;
public sealed interface TaskBody<Id, Definition, Type extends Enum<Type> & TaskType> permits LeafBody, NodeBody {//<Id, Definition, Type extends Enum<Type> & TaskType> {
//    void perform(StackedExecutor<Id, Definition, Type> executor, Storage storage);
}
