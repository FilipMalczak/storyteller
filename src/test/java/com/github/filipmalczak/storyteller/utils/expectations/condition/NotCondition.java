package com.github.filipmalczak.storyteller.utils.expectations.condition;

import lombok.Value;

@Value
class NotCondition<E, T> implements Condition<E, T> {
    Condition<E, T> delegate;

    @Override
    public boolean isSatisfied(E expected, T tracked) {
        return !delegate.isSatisfied(expected, tracked);
    }

    @Override
    public String describe() {
        return "not "+delegate.describe();
    }

    @Override
    public Condition<E, T> not() {
        return delegate;
    }
}
