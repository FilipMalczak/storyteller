package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.Value;
import lombok.experimental.PackagePrivate;

import java.util.List;
import java.util.function.BiPredicate;

import static java.util.stream.Collectors.joining;

@Value
class OrderedGroup<E> implements Expectation<E> {
    @PackagePrivate List<Expectation<E>> expectations;

    @Override
    public <T> boolean match(T tracked, BiPredicate<E, T> checker) {
        return expectations.get(0).match(tracked, checker);
    }

    @Override
    public boolean isFullySatisfied() {
        return expectations.isEmpty();
    }

    @Override
    public void step() {
        var next = expectations.get(0);
        next.step();
        if (next.isFullySatisfied())
            expectations.remove(0);
    }

    @Override
    public String describe() {
        return "(" + expectations.stream().map(Expectation::describe).collect(joining(", then ")) + ")";
    }

    @Override
    public String describeDirectNext() {
        return expectations.get(0).describeDirectNext();
    }
}
