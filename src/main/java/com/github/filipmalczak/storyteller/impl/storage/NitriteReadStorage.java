package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.storage.ReadFilesApi;
import com.github.filipmalczak.storyteller.storage.ReadStorage;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;

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
