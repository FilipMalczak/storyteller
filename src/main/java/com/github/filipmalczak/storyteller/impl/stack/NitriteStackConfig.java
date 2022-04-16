package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import lombok.Value;

@Value
public class NitriteStackConfig<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    NitriteStorageConfig<Id> storageConfig;
    IdGeneratorFactory<Id, Definition, Type> generatorFactory;

    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
        NitriteStackConfig<Id, Definition, Type> of(NitriteStorageConfig<Id> storageConfig,
                                                    IdGeneratorFactory<Id, Definition, Type> generatorFactory){
        return new NitriteStackConfig<>(storageConfig, generatorFactory);
    }
}
