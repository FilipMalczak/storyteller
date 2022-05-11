package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpectationNotMetException extends AssertionError {
    CallbackContext context;

    public ExpectationNotMetException(CallbackContext context) {
        super("Following object didn't match the condition: "+context.tracked()+"\n"+
            "\t> Condition was: "+context.condition()+"\n"+
            "\t> Direct expectation was: "+context.directExpectation()+"\n"+
            "\t> All the expectations were: "+context.expectation()+"\n");
        this.context = context;
    }
}
