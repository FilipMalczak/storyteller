package com.github.filipmalczak.storyteller.impl.storage.config;

import lombok.NonNull;
import lombok.Value;

import java.nio.file.Path;

@Value
public class NitriteStorageConfig<Id extends Comparable<Id>> {
    @NonNull Path dataStorage;
    @NonNull IdSerializer<Id> serializer;
}
