package com.github.filipmalczak.storyteller.utils.expectations;

import java.util.function.BiPredicate;

public interface Condition<E, T> extends Describable {
    boolean isSatisfied(E expected, T tracked);

    static <E, T> Condition<E, T> of(BiPredicate<E, T> predicate, String desc) {
        return new Condition<>() {
            @Override
            public boolean isSatisfied(E expected, T tracked) {
                return predicate.test(expected, tracked);
            }

            @Override
            public String describe() {
                return desc;
            }
        };
    }
}
