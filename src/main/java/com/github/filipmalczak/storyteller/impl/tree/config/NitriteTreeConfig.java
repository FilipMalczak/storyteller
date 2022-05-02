package com.github.filipmalczak.storyteller.impl.tree.config;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;

public record NitriteTreeConfig<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>(
    NitriteStorageConfig<Id> storageConfig,
    IdGeneratorFactory<Id, Definition, Type> generatorFactory,
    MergeSpecFactory<Id, Definition, Type> mergeSpecFactory
) {
    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    NitriteTreeConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                               IdGeneratorFactory<Id, Definition, Type> generatorFactory,
                                               MergeSpecFactory<Id, Definition, Type> mergeSpecFactory){
        return new NitriteTreeConfig<>(storageConfig, generatorFactory, mergeSpecFactory);
    }
}
