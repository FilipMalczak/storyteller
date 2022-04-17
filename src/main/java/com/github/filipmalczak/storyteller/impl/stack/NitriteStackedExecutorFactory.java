package com.github.filipmalczak.storyteller.impl.stack;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.StackedExecutorFactory;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import org.dizitart.no2.Nitrite;

import java.util.LinkedList;

public class NitriteStackedExecutorFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    implements StackedExecutorFactory<Id, Definition, Type, Nitrite, NitriteStackConfig<Id, Definition, Type>> {
    public StackedExecutor<Id, Definition, Type, Nitrite>
        create(NitriteStackConfig<Id, Definition, Type> config){
        var nitriteFile = config.getStorageConfig().getDataStorage().resolve("index.no2");
        var no2 = Nitrite.builder()
            .compressed()
            .filePath(nitriteFile.toFile())
            .registerModule(new JavaTimeModule())
            .openOrCreate();
        var managers = new NitriteManagers<Id, Definition, Type>(no2);
        return new NitriteStackedExecutor<Id, Definition, Type>(
            managers,
            new HistoryTracker<>(managers.getHistoryManager()),
            config.getStorageConfig(),
            config.getGeneratorFactory(),
            new LinkedList<>()
        );
    }
}
