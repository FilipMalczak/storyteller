package com.github.filipmalczak.storyteller.impl.visualize.start;

import com.github.filipmalczak.storyteller.api.visualize.StartingPoint;
import lombok.Value;

@Value
public final class FromId<Id> implements StartingPoint<Id> {
    Id id;

    @Override
    public Id get() {
        return id;
    }
}
