package com.github.filipmalczak.storyteller.impl.tree.internal.history;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public sealed interface IncrementalHistoryTracker<Id> extends HistoryTracker<Id> permits IncrementalHistoryTrackerImpl {
    default Map<Id, HistoryDiff<Id>> getIncrement(){
        var out = new HashMap<Id, HistoryDiff<Id>>();
        getChangedSubjects().forEach(subject -> out.put(subject, getIncrement(subject)));
        return out;
    }

    HistoryDiff<Id> getIncrement(Id subject);
    Stream<Id> getChangedSubjects();


}
