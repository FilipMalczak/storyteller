package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.function.BiPredicate;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@EqualsAndHashCode(of = "identity")
@ToString(exclude = "identity")
final class ConcreteExpectation<E> implements Expectation<E> {
    final int identity = IdentityHelper.i.getAndIncrement();
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
        return "expect " + expected.toString();
    }

    @Override
    public String describeDirectNext() {
        return describe();
    }
}
