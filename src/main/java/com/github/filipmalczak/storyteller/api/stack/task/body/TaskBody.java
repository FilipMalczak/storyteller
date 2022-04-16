package com.github.filipmalczak.storyteller.api.stack.task.body;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;

public sealed interface TaskBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> permits LeafBody, NodeBody {
}
