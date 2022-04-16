package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.storage.files.SimpleReadFiles;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class NitriteReadStorage<Id> implements ReadStorage {
    @NonNull NitriteStorageConfig<Id> config;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull Id current;

    @Override
    public ReadFilesApi files() {
        return new SimpleReadFiles(config, tracker, current);
    }
}
