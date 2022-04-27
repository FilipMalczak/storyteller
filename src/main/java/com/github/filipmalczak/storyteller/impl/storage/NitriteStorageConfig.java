package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.tree.task.id.IdSerializer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.nio.file.Path;

@Value
public class NitriteStorageConfig<Id extends Comparable<Id>> {
    @NonNull Path dataStorage;
    @NonNull IdSerializer<Id> serializer;
    boolean enableNo2OffHeapStorage;

    public NitriteStorageConfig(@NonNull Path dataStorage, @NonNull IdSerializer<Id> serializer, boolean enableNo2OffHeapStorage) {
        this.dataStorage = dataStorage.toAbsolutePath();
        this.serializer = serializer;
        this.enableNo2OffHeapStorage = enableNo2OffHeapStorage;
    }

    public NitriteStorageConfig(@NonNull Path dataStorage, @NonNull IdSerializer<Id> serializer) {
        this(dataStorage, serializer, false);
    }
}
