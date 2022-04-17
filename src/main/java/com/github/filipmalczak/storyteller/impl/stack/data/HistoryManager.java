package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.impl.stack.TaskHistory;

import java.util.List;

public interface HistoryManager<Id> {
    void persist(Id taskId, TaskHistory<Id> history);

}
