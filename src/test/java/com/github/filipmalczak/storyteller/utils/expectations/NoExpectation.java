package com.github.filipmalczak.storyteller.utils.expectations;

import java.util.function.BiPredicate;

final class NoExpectation<E> implements Expectation<E> {
    @Override
    public String describeDirectNext() {
        return "never mind";
    }

    @Override
    public String describe() {
        return "never mind";
    }

    @Override
    public <T> boolean match(T tracked, BiPredicate<E, T> checker) {
        return true;
    }

    @Override
    public void step() {

    }

    @Override
    public boolean isFullySatisfied() {
        return false;
    }
}
