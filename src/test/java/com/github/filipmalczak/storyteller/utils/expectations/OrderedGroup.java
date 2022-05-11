package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.*;
import lombok.experimental.PackagePrivate;

import java.util.List;
import java.util.function.BiPredicate;

import static java.util.stream.Collectors.joining;

@ToString(exclude = "identity")
@EqualsAndHashCode(of = "identity")
@RequiredArgsConstructor
final class OrderedGroup<E> implements Expectation<E> {
    final int identity = IdentityHelper.i.getAndIncrement();
    @NonNull
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
        return expectations.isEmpty() ? "N/A" : expectations.get(0).describeDirectNext();
    }
}
