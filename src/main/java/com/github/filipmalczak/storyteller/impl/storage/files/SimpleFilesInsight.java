package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.NonNull;

import java.util.stream.Stream;

public class SimpleFilesInsight<Id extends Comparable<Id>> extends SimpleReadFiles<Id> {
    public SimpleFilesInsight(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current) {
        super(config, tracker, current);
    }

    protected Stream<Id> getLeavesHistory(){
        return Stream.concat(Stream.of(current), tracker.getWritingAncestors(current));
    }
}
