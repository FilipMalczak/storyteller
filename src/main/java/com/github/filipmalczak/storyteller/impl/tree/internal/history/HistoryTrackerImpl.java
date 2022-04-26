package com.github.filipmalczak.storyteller.impl.tree.internal.history;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class HistoryTrackerImpl<Id> implements HistoryTracker<Id> {
    @NonNull final Map<Id, TaskHistory<Id>> backend;

    @Override
    public void startFrom(Id taskId, Id startPoint){
        backend.put(taskId, backend.getOrDefault(startPoint, TaskHistory.empty()).copy());
    }

    @Override
    public void startFromScratch(Id taskId){
        backend.put(taskId, TaskHistory.empty());
    }

    /**
     * Adds as the last
     */
    @Override
    public void add(Id taskId, Id toAdd, boolean isLeaf){
        if (!backend.containsKey(taskId))
            backend.put(taskId, TaskHistory.empty());
        backend.get(taskId).add(toAdd, isLeaf);
    }

    /**
     * First element is the previous task id; last element is the root task id.
     */
    @Override
    public Stream<Id> getAllAncestors(Id taskId){
        if (backend.containsKey(taskId))
            return backend.get(taskId).getFull().stream();
        return Stream.empty();
    }

    /**
     * First element is the last leaf id; last element is the first leaf id.
     */
    @Override
    public Stream<Id> getLeafAncestors(Id taskId){
        if (backend.containsKey(taskId))
            return backend.get(taskId).getLeaves().stream();
        return Stream.empty();
    }

    @Override
    public void apply(Id subject, HistoryDiff<Id> diff) {
        var currentHistory = backend.getOrDefault(subject, TaskHistory.empty());
        var updatedHistory = new TaskHistory<>(
            new LinkedList<>(
                Stream.concat(
                    diff.getAllAddedAncestors().stream(),
                    currentHistory.getFull().stream()
                ).toList()
            ),
            new LinkedList<>(
                Stream.concat(
                    diff.getAddedLeafAncestors().stream(),
                    currentHistory.getLeaves().stream()
                ).toList()
            )
        );
        backend.put(subject, updatedHistory);
    }

    public static <Id> HistoryTracker<Id> empty(){
        return new HistoryTrackerImpl<>(new HashMap<>());
    }
}
