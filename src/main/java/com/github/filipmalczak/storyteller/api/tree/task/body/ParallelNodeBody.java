package com.github.filipmalczak.storyteller.api.tree.task.body;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Incorporate;
import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Insight;

public interface ParallelNodeBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    void perform(TaskTree<Id, Definition, Type, NoSql> executor,
                 ReadStorage<NoSql> storage,
                 Insight<Id, Definition, Type, NoSql> insights,
                 Incorporate<Id, Definition, Type> incorporate);
}
