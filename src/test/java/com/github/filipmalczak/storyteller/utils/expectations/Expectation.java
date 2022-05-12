package com.github.filipmalczak.storyteller.utils.expectations;

import java.util.function.BiPredicate;

public sealed interface Expectation<E> extends DeepDescribable permits ConcreteExpectation, OrderedGroup, UnorderedGroup {
    <T> boolean match(T tracked, BiPredicate<E, T> checker);

    void step();

    boolean isFullySatisfied();
}
