package com.github.filipmalczak.storyteller.api.tree.task.body.handles;

import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

public interface Incorporate<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    void subtask(Task<Id, Definition, Type> subtask);
}
