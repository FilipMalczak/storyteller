package com.github.filipmalczak.storyteller.utils.expectations;

import java.util.function.Consumer;

public interface Callback<T> {
    void run(CallbackContext<T> context);

    static <T> Callback<T> logSuccess(Consumer<String> logger) {
        return ctx -> {
            logger.accept("Following object matched the condition: " + ctx.tracked());
            logger.accept("\t> Condition was: " + ctx.condition());
            logger.accept("\t> Direct expectation was: " + ctx.directExpectation());
            logger.accept("\t> All the expectations were: " + ctx.expectation());
        };
    }
}
