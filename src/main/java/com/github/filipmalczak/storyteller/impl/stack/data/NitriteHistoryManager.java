package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.impl.stack.TaskHistory;
import com.github.filipmalczak.storyteller.impl.stack.data.model.TaskHistoryData;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.List;

public class NitriteHistoryManager<Id extends Comparable<Id>> implements HistoryManager<Id> {
    @Setter(AccessLevel.PACKAGE) @NonNull ObjectRepository<TaskHistoryData> repository;
    @Setter(AccessLevel.PACKAGE) @NonNull SessionManager sessionManager;

    @Override
    public void persist(Id taskId, TaskHistory<Id> history) {
        var data = new TaskHistoryData<>(
            taskId,
            sessionManager.getCurrent().getId(),
            history
        );
        repository.update(data, true);
    }
}
