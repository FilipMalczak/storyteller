package com.github.filipmalczak.storyteller.utils;

import com.github.filipmalczak.storyteller.api.session.Sessions;
import com.github.filipmalczak.storyteller.utils.expectations.Callback;
import com.github.filipmalczak.storyteller.utils.expectations.StructuredExpectations;
import com.github.filipmalczak.storyteller.utils.expectations.condition.Condition;
import com.github.filipmalczak.storyteller.utils.expectations.exception.ExpectationNotMetException;
import com.github.filipmalczak.storyteller.utils.expectations.exception.UnknownExpectations;
import com.github.filipmalczak.storyteller.utils.expectations.exception.UnsatisfiedExpectationsLeft;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.flogger.Flogger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class ExecutionTracker<T> {
    List<T> data = new ArrayList<>();
    @Getter @Setter
    @NonFinal Sessions sessions;

    public void mark(T event){
        log.atInfo().log("tracker.mark(%s)", event);
        data.add(event);
    }

    public void clear(){
        log.atInfo().log("tracker.clear()");
        data.clear();
        if (sessions != null)
            sessions.end();
    }

    public void expect(Object... events){
        log.atInfo().log("tracker.expect(%s)", asList(events));
        StructuredExpectations.<T, T>builder()
            .condition(Condition.of(Object::equals, "elements must be equal"))
            .onMatch(Callback.logSuccess(log.atInfo()::log))
            .onMismatch(ctx -> {throw new ExpectationNotMetException(ctx); })
            .onLeftovers(ctx -> {throw new UnsatisfiedExpectationsLeft(ctx); })
            .onMissingInstructions(cause -> {throw new UnknownExpectations(cause); })
            .build()
            .expect(events)
            .matchAll(data);
    }
}
