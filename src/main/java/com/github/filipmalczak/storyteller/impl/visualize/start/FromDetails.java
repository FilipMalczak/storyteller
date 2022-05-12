package com.github.filipmalczak.storyteller.impl.visualize.start;

import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.api.visualize.StartingPoint;
import lombok.Value;

@Value
public final class FromDetails<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements StartingPoint<Id> {
    IdGeneratorFactory<Id, Definition, Type> factory;
    TaskSpec<Definition, Type> spec;

    @Override
    public Id get() {
        return factory.over(spec).generate();
    }
}
