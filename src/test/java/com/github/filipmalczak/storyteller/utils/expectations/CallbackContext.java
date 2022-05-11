package com.github.filipmalczak.storyteller.utils.expectations;

public record CallbackContext<T>(T tracked, String condition, String expectation, String directExpectation) {
}
