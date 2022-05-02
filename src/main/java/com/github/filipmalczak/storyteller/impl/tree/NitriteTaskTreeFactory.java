package com.github.filipmalczak.storyteller.impl.tree;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeFactory;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.config.NitriteTreeConfig;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import org.dizitart.no2.Nitrite;

import java.util.LinkedList;

public class NitriteTaskTreeFactory<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    implements TaskTreeFactory<Id, Definition, Type, Nitrite, NitriteTreeConfig<Id, Definition, Type>> {
    public TaskTreeRoot<Id, Definition, Type, Nitrite>
        create(NitriteTreeConfig<Id, Definition, Type> config){
        config.storageConfig().getDataStorage().toFile().mkdirs();
        var nitriteFile = config.storageConfig().getDataStorage().resolve("index.no2");
        var no2 = Nitrite.builder()
            .compressed()
            .filePath(nitriteFile.toFile())
            .registerModule(new JavaTimeModule())
            .openOrCreate();
        var managers = new NitriteManagers<Id, Definition, Type>(no2);
        return new NitriteTaskTree<>(
            managers,
            HistoryTracker.get(),
            config.storageConfig(),
            config.generatorFactory(),
            config.mergeSpecFactory(),
            new LinkedList<>(),
            true
        );
    }
}
