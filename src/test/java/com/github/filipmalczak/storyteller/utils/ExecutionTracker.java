package com.github.filipmalczak.storyteller.utils;

import com.github.filipmalczak.storyteller.api.session.Sessions;
import com.github.filipmalczak.storyteller.utils.expectations.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.flogger.Flogger;

import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.valid4j.Assertive.require;

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
