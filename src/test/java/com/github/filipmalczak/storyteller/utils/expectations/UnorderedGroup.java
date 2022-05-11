package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.stream.Collectors.joining;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
final class UnorderedGroup<E> implements Expectation<E> {
    @NonNull Set<Expectation<E>> expected;
    Expectation<E> next = null;

    @Override
    public <T> boolean match(T tracked, BiPredicate<E, T> checker) {
        if (next == null) {
            var candidateForNext = expected.stream().filter(e -> e.match(tracked, checker)).findFirst();
            if (candidateForNext.isEmpty()) {
                return false;
            }
            next = candidateForNext.get();
        }
        return next.match(tracked, checker);
    }

    @Override
    public void step() {
        next.step();
        if (next.isFullySatisfied()) {
            expected.remove(next);
            next = null;
        }
    }

    @Override
    public boolean isFullySatisfied() {
        return expected.isEmpty();
    }

    @Override
    public String describe() {
        if (next == null) {
            return "(" + expected.stream().map(Expectation::describe).collect(joining(", or ")) + ")";
        }
        return "(" + next.describe() + ", then " + expected.stream().filter(x -> x != next).map(Expectation::describe).collect(joining(", or ")) + ")";
    }

    @Override
    public String describeDirectNext() {
        if (next == null) {
            return expected.stream().map(Expectation::describeDirectNext).collect(joining(", or"));
        }
        return next.describeDirectNext();
    }
}
