package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.impl.tree.internal.TaskHistory;

public interface HistoryManager<Id> {
    void persist(Id taskId, TaskHistory<Id> history);

}
