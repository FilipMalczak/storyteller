package com.github.filipmalczak.storyteller.impl.tree.internal;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
public class HistoryTracker<Id> {
    @NonNull final Map<Id, TaskHistory<Id>> backend;

    public HistoryTracker() {
        this(new HashMap<>());
    }

    public HistoryTracker(HistoryTracker<Id> other) {
        this(copy(other.backend));
    }

    private static <Id> Map<Id, TaskHistory<Id>> copy(Map<Id, TaskHistory<Id>> map){
        var out = new HashMap<Id, TaskHistory<Id>>();
        for (var k: map.keySet())
            out.put(k, map.get(k).copy());
        return out;
    }

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

    public HistoryTracker<Id> copy(){
        return new HistoryTracker<>(this);
    }

    public void mirror(HistoryTracker<Id> another){
        //todo not the nicest way to do that
        for (var k: another.backend.keySet())
            backend.put(k, another.backend.get(k));
    }
}
