package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.files.IndexedReadFiles;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.dizitart.no2.Nitrite;

import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.load;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteInsight<Id extends Comparable<Id>> extends AbstractNitriteStorage<Id> {
    @NonNull IndexedReadFiles readFiles;

    NitriteInsight(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current, @NonNull IndexedReadFiles<Id> readFiles) {
        super(config, tracker, current);
        this.readFiles = readFiles;
    }

    @Override
    public ReadFilesApi files() {
        return readFiles;
    }

}
