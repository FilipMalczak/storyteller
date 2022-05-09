package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.tree.task.body.handles.Insight;
import com.github.filipmalczak.storyteller.impl.storage.files.FilesApiFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

import java.util.Map;

import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteStorageFactory<Id extends Comparable<Id>> {
    @NonNull Nitrite db;
    @Getter @NonNull NitriteStorageConfig<Id> config;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull FilesApiFactory<Id> filesApiFactory;

    public NitriteStorageFactory(@NonNull Nitrite db, @NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker) {
        this.db = db;
        this.config = config;
        this.tracker = tracker;
        filesApiFactory = new FilesApiFactory<>(db, tracker, config);
    }

    public NitriteReadStorage<Id> read(Id id){
        return new NitriteReadStorage<>(config, tracker, id, filesApiFactory.read(id));
    }

    public NitriteReadWriteStorage<Id> readWrite(Id id){
        return new NitriteReadWriteStorage<>(config, tracker, id, filesApiFactory.readWrite(id));
    }

    public NitriteParallelStorage<Id> parallelRead(Id id){
        return new NitriteParallelStorage<>(config, tracker, id, filesApiFactory.readWrite(id));
    }


    public Insight<Id, Nitrite> insights(Map<Id, Map<Id, HistoryDiff<Id>>> increments){
        var frozen = tracker.snapshot();
        return (Id id) -> {
            var snapshot = frozen.snapshot();
            require(increments.containsKey(id), "Insights are only available for subtasks"); //todo subtaskMissingException
            var increment = increments.get(id);
            snapshot.apply(increment);
            return new NitriteInsight<>(config, snapshot, id, new FilesApiFactory<>(db, snapshot, config).insight(id));
        };
    }
}
