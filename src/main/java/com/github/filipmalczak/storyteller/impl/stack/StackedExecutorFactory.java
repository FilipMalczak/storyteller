package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.stack.data.HistoryManager;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.stack.task.TaskType;
import org.dizitart.no2.Nitrite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class StackedExecutorFactory {
    public <Id, Definition, Type extends Enum<Type> & TaskType> StackedExecutor<Id, Definition, Type>
        create(NitriteStorageConfig storageConfig, IdGeneratorFactory<Id, Definition, Type> generatorFactory){
        var nitriteFile = storageConfig.getDataStorage().resolve("index.no2");
        var no2 = Nitrite.builder()
            .compressed()
            .filePath(nitriteFile.toFile())
            .openOrCreate();
        var managers = new NitriteManagers<Id, Definition, Type>(no2);
        return new NitriteStackedExecutor<Id, Definition, Type>(
            managers,
            new HistoryTracker<>(managers.getHistoryManager()),
            storageConfig,
            generatorFactory,
            new ArrayList<>(),
            Optional.empty(),
            new LinkedList<>()
        );
    }
}
