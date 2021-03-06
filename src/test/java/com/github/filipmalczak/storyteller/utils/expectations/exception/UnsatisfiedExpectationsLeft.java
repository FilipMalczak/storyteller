package com.github.filipmalczak.storyteller.utils.expectations.exception;

import com.github.filipmalczak.storyteller.utils.expectations.CallbackContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnsatisfiedExpectationsLeft extends AssertionError {
    CallbackContext context;

    public UnsatisfiedExpectationsLeft(CallbackContext context) {
        super("Some expectations were still not satisfied!\n"+
            "\t> Condition was: "+context.condition()+"\n"+
            "\t> Direct expectation was: "+context.directExpectation()+"\n"+
            "\t> All the expectations were: "+context.expectation()+"\n");
        this.context = context;
    }
}
