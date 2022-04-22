package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import lombok.Value;

@Value
public class NitriteTreeConfig<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    NitriteStorageConfig<Id> storageConfig;
    IdGeneratorFactory<Id, Definition, Type> generatorFactory;

    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    NitriteTreeConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                               IdGeneratorFactory<Id, Definition, Type> generatorFactory){
        return new NitriteTreeConfig<>(storageConfig, generatorFactory);
    }
}
