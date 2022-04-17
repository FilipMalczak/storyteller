package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.stack.data.HistoryManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HistoryTracker<Id> {
    @NonNull final Map<Id, TaskHistory<Id>> backend = new HashMap<>();
    @NonNull final HistoryManager<Id> manager;

    /**
     * Assumes the same order of history as in get()
     */
//    public void put(Id taskId, TaskHistory<Id> history){
//        backend.put(taskId, history.copy());
//        persist(taskId);
//    }

    public void startFrom(Id taskId, Id startPoint){
        backend.put(taskId, backend.getOrDefault(startPoint, new TaskHistory<>()).copy());
    }

    public void startFromScratch(Id taskId){
        backend.put(taskId, new TaskHistory<>());
    }

    public void start(Id taskId, Optional<Id> startPoint){
        if (startPoint.isPresent())
            startFrom(taskId, startPoint.get());
        else
            startFromScratch(taskId);
    }

    /**
     * Adds as the last
     */
    public void add(Id taskId, Id toAdd, boolean isLeaf){
        if (!backend.containsKey(taskId))
            backend.put(taskId, new TaskHistory<>());
        backend.get(taskId).add(toAdd, isLeaf);
        persist(taskId);
    }

    private void persist(Id taskId){
        manager.persist(taskId, backend.get(taskId));
    }

    /**
     * First element is the previous task id; last element is the root task id.
     */
    public Stream<Id> getFull(Id taskId){
        if (backend.containsKey(taskId))
            return backend.get(taskId).getFull().stream();
        return Stream.empty();
    }

    /**
     * First element is the last leaf id; last element is the first leaf id.
     */
    public Stream<Id> getLeaves(Id taskId){
        if (backend.containsKey(taskId))
            return backend.get(taskId).getLeaves().stream();
        return Stream.empty();
    }
}
