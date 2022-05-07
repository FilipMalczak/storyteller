package com.github.filipmalczak.storyteller.impl.tree.internal.history;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public sealed interface HistoryTracker<Id> permits IncrementalHistoryTracker, HistoryTrackerImpl {
    void startFrom(Id taskId, Id startPoint);

    /**
     * Adds as the last
     */
    void add(Id taskId, Id toAdd, boolean isWriting);

    /**
     * First element is the previous task id; last element is the root task id.
     */
    Stream<Id> getAllAncestors(Id taskId);

    /**
     * First element is the last leaf id; last element is the first leaf id.
     */
    Stream<Id> getWritingAncestors(Id taskId);

    void apply(Id subject, HistoryDiff<Id> diff);

    default void apply(Map<Id, HistoryDiff<Id>> diff){
        for (var subject: diff.keySet()) {
            apply(subject, diff.get(subject));
        }
    }

    static <Id> HistoryTracker<Id> get(){
        return HistoryTrackerImpl.empty();
    }

    default IncrementalHistoryTracker<Id> snapshot(){
        return IncrementalHistoryTrackerImpl.of(this);
    }
}
