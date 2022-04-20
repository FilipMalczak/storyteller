package com.github.filipmalczak.storyteller.impl.storage.config;

import com.github.filipmalczak.storyteller.api.stack.task.id.IdSerializer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.nio.file.Path;

@Value
@AllArgsConstructor
public class NitriteStorageConfig<Id extends Comparable<Id>> {
    @NonNull Path dataStorage;
    @NonNull IdSerializer<Id> serializer;
    boolean enableNo2OffHeapStorage;

    public NitriteStorageConfig(@NonNull Path dataStorage, @NonNull IdSerializer<Id> serializer) {
        this(dataStorage, serializer, false);
    }
}
