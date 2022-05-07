package com.github.filipmalczak.storyteller.impl.tree.config;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;

import java.util.Comparator;

public record NitriteTreeConfig<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>(
    NitriteStorageConfig<Id> storageConfig,
    IdGeneratorFactory<Id, Definition, Type> generatorFactory,
    MergeSpecFactory<Id, Definition, Type> mergeSpecFactory,
    Comparator<Id> mergeOrder
) {
    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    NitriteTreeConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                               IdGeneratorFactory<Id, Definition, Type> generatorFactory,
                                               MergeSpecFactory<Id, Definition, Type> mergeSpecFactory,
                                               Comparator<Id> mergeOrder){
        return new NitriteTreeConfig<>(storageConfig, generatorFactory, mergeSpecFactory, mergeOrder);
    }

    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    NitriteTreeConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                               IdGeneratorFactory<Id, Definition, Type> generatorFactory,
                                               MergeSpecFactory<Id, Definition, Type> mergeSpecFactory){
        return of(storageConfig, generatorFactory, mergeSpecFactory, Comparable::compareTo);
    }
}
