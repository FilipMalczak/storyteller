package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.storage.files.FilesApiFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteStorageFactory<Id extends Comparable<Id>> {
    @Getter @NonNull NitriteStorageConfig<Id> config;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull FilesApiFactory<Id> filesApiFactory;

    public NitriteStorageFactory(@NonNull Nitrite db, @NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker) {
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

    public NitriteInsight<Id> insight(Id id){
        return new NitriteInsight<>(config, tracker, id, filesApiFactory.insight(id));
    }
}
