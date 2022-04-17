package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.impl.stack.TaskHistory;

public interface HistoryManager<Id> {
    void persist(Id taskId, TaskHistory<Id> history);

}
