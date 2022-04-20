package com.github.filipmalczak.storyteller.impl.visualize.start;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.api.visualize.StartingPoint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class StartingPoints {
    public static <Id extends Comparable<Id>> StartingPoint<Id> of(Id id) {
        return new FromId<>(id);
    }

    public static <Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    StartingPoint<Id> of(IdGeneratorFactory<Id, Definition, Type> factory, Definition definition, Type type) {
        return new FromDetails<>(factory, definition, type);
    }
}
