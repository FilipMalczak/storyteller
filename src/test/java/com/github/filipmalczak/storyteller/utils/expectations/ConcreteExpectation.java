package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.function.BiPredicate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
final class ConcreteExpectation<E> implements Expectation<E> {
    @NonNull E expected;
    @Getter
    boolean fullySatisfied = false;

    @Override
    public <T> boolean match(T tracked, BiPredicate<E, T> checker) {
        return checker.test(expected, tracked);
    }

    @Override
    public void step() {
        fullySatisfied = true;
    }

    @Override
    public String describe() {
        return "expect: " + expected.toString();
    }

    @Override
    public String describeDirectNext() {
        return describe();
    }
}
