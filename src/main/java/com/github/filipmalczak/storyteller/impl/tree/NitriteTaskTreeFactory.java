package com.github.filipmalczak.storyteller.impl.tree;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeFactory;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.config.NitriteTreeConfig;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.NullContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.RootAdapter;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.TaskExecutorImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.EventsEmitter;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.JournalEntryFactory;
import org.dizitart.no2.Nitrite;

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
        var treeContext = new TreeContext<>(
            managers,
            new EventsEmitter<>(managers.getEventsPersistence(), new JournalEntryFactory(managers.getSessionManager())),
            config.storageConfig(),
            config.idGeneratorFactory(),
            config.mergepecFactory(),
            config.mergeOrder()
        );
        return new RootAdapter<>(managers.getSessionManager(), new TaskExecutorImpl<>(treeContext, new NullContext<>()));
    }
}
