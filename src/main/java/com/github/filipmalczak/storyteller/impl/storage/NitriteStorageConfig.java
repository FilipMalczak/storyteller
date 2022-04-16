package com.github.filipmalczak.storyteller.impl.storage;

import lombok.NonNull;
import lombok.Value;

import java.nio.file.Path;

@Value
public class NitriteStorageConfig<Id> {
    @NonNull Path dataStorage;
    @NonNull IdSerializer<Id> serializer;
}
