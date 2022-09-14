package com.github.filipmalczak.storyteller.impl.nextgen;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdSerializer;
import com.github.filipmalczak.storyteller.impl.tree.config.TaskpecFactory;
import lombok.Builder;
import lombok.NonNull;

import java.util.Comparator;

@Builder
public record NitriteTreeConfig<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> (
    @NonNull IdGeneratorFactory<Id, Definition, Type> idGeneratorFactory,
    @NonNull TaskpecFactory<Id, Definition, Type> mergepecFactory,
    @NonNull Comparator<Id> mergeOrder,
    @NonNull IdSerializer<Id> serializer,
    boolean enableNo2OffHeapStorage
){
}
