package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.files.SimpleFilesInsight;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.dizitart.no2.Nitrite;

import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.load;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InsightIntoNitriteStorage<Id extends Comparable<Id>> implements ReadStorage<Nitrite> {

    @NonNull NitriteStorageConfig<Id> config;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull Id current;
    @NonFinal
    Nitrite nitrite;

    public InsightIntoNitriteStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current) {
        this.config = config;
        this.tracker = tracker;
        this.current = current;
        nitrite = load(config, Optional.of(current));
    }

    @Override
    public ReadFilesApi files() {
        return new SimpleFilesInsight<>(config, tracker, current);
    }

    @Override
    public Nitrite documents() {
        return nitrite;
    }
}
