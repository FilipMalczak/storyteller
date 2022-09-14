package com.github.filipmalczak.storyteller.api.session;

import com.github.filipmalczak.storyteller.api.tree.TaskResolver;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Insight;

public non-sealed interface PastSession<TaskId extends Comparable<TaskId>, NoSql> extends Session<TaskId>{
    TaskResolver<TaskId, ?, ?> getTaskResolver();
    Insight<TaskId, NoSql> getFinalInsights();
}
