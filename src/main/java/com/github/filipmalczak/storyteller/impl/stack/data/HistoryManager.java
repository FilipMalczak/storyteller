package com.github.filipmalczak.storyteller.impl.stack.data;

import java.util.List;

public interface HistoryManager<Id> {
    void persist(Id taskId, List<Id> history);
}
