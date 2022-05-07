package com.github.filipmalczak.storyteller.impl.tree.internal.history;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncrementalHistoryTrackerImpl<Id> implements IncrementalHistoryTracker<Id> {
    HistoryTracker<Id> past;
    Map<Id, HistoryDiff<Id>> increments;


    @Override
    public void startFrom(Id taskId, Id startPoint) {
        var all = getAllAncestors(startPoint).toList();
        var leaves = getWritingAncestors(startPoint).toList();
        increments.put(taskId, new HistoryDiff<>(all, leaves));
    }

    @Override
    public void startFromScratch(Id taskId) {
        throw new UnsupportedOperationException("Incremental tracker cannot be used to start from scratch!");
    }

    @Override
    public void add(Id taskId, Id toAdd, boolean isWriting) {
        increments.put(taskId, getIncrement(taskId).and(toAdd, isWriting));
    }

    @Override
    public Stream<Id> getAllAncestors(Id taskId) {
        return Stream.concat(
            getIncrement(taskId).getAddedAncestors().stream(),
            past.getAllAncestors(taskId)
        );
    }

    @Override
    public Stream<Id> getWritingAncestors(Id taskId) {
        return Stream.concat(
            getIncrement(taskId).getAddedWritingAncestors().stream(),
            past.getWritingAncestors(taskId)
        );
    }

    @Override
    public void apply(Id subject, HistoryDiff<Id> diff) {
        increments.put(subject, getIncrement(subject).and(diff));
    }

    @Override
    public HistoryDiff<Id> getIncrement(Id subject) {
        return increments.getOrDefault(subject, HistoryDiff.empty());
    }

    @Override
    public Stream<Id> getChangedSubjects() {
        return increments.keySet().stream();
    }

    public static <Id> IncrementalHistoryTracker<Id> of(HistoryTracker<Id> past){
        return new IncrementalHistoryTrackerImpl<>(past, new HashMap<>());
    }
}
