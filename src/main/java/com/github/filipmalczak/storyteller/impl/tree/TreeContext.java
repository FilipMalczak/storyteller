package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.tree.config.MergeSpecFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.ExecutionFactory;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.ExecutionFactoryImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.EventsEmitter;
import lombok.Getter;
import lombok.Value;
import org.dizitart.no2.Nitrite;

import java.util.Comparator;

@Value
public class TreeContext<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    NitriteManagers<Id, Definition, Type> nitriteManagers;
    EventsEmitter<Id> events;
    NitriteStorageConfig<Id> storageConfig;
    IdGeneratorFactory<Id, Definition, Type> generatorFactory;
    MergeSpecFactory<Id, Definition, Type> mergeSpecFactory;
    Comparator<Id> mergeOrder;
    @Getter(lazy = true)
    //todo if we add nosql generics, we can abstract [managers, merging, importing, exporting] out and we may have a pluggable task tree
    ExecutionFactory<Id, Definition, Type, Nitrite> executionFactory = new ExecutionFactoryImpl<>(this);
}
