package com.github.filipmalczak.storyteller.impl.tree.config;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;

import java.util.Comparator;

public record NitriteTreeConfig<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>(
    NitriteStorageConfig<Id> storageConfig,
    IdGeneratorFactory<Id, Definition, Type> generatorFactory,
    TaskpecFactory<Id, Definition, Type> taskpecFactory,
    Comparator<Id> mergeOrder
) {
    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    NitriteTreeConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                               IdGeneratorFactory<Id, Definition, Type> generatorFactory,
                                               TaskpecFactory<Id, Definition, Type> taskpecFactory,
                                               Comparator<Id> mergeOrder){
        return new NitriteTreeConfig<>(storageConfig, generatorFactory, taskpecFactory, mergeOrder);
    }

    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    NitriteTreeConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                               IdGeneratorFactory<Id, Definition, Type> generatorFactory,
                                               TaskpecFactory<Id, Definition, Type> taskpecFactory){
        return of(storageConfig, generatorFactory, taskpecFactory, Comparable::compareTo);
    }
}
