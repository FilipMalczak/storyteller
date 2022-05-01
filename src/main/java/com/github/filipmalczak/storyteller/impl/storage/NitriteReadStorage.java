package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.files.IndexedReadFiles;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class NitriteReadStorage<Id extends Comparable<Id>> extends AbstractNitriteStorage<Id> {
    @NonNull IndexedReadFiles<Id> readFiles;

    NitriteReadStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current, @NonNull IndexedReadFiles<Id> readFiles) {
        super(config, tracker, current);
        this.readFiles = readFiles;
    }

    @Override
    public ReadFilesApi files() {
        return readFiles;
    }
}
