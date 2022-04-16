package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.stack.data.HistoryManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HistoryTracker<Id> {
    @NonNull final Map<Id, LinkedList<Id>> backend = new HashMap<>();
    @NonNull final HistoryManager<Id> manager;

    /**
     * Assumes the same order of history as in get()
     */
    public void put(Id taskId, List<Id> history){
        backend.put(taskId, new LinkedList<>(history));
        persist(taskId);
    }

    /**
     * Adds as the last
     */
    public void add(Id taskId, Id toAdd){
        if (!backend.containsKey(taskId))
            backend.put(taskId, new LinkedList<>());
        backend.get(taskId).addFirst(toAdd);
        persist(taskId);
    }

    private void persist(Id taskId){
        manager.persist(taskId, backend.get(taskId));
    }

    /**
     * First element is the previous task id; last element is the root task id.
     */
    public Stream<Id> get(Id taskId){
        return backend.getOrDefault(taskId, new LinkedList<>()).stream();
    }
}
