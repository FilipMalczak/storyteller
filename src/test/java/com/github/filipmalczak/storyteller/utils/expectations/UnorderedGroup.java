package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.stream.Collectors.joining;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Flogger
@ToString(exclude = "identity")
@EqualsAndHashCode(of = "identity")
final class UnorderedGroup<E> implements Expectation<E> {
    final int identity = IdentityHelper.i.getAndIncrement();
    @NonNull Set<Expectation<E>> expected;
    Expectation<E> next = null;

    @Override
    public <T> boolean match(T tracked, BiPredicate<E, T> checker) {
        if (next == null) {
            log.atFiner().log("No next expectation set, looking for a candidate");
            var candidateForNext = expected.stream().filter(e -> e.match(tracked, checker)).findFirst();
            if (candidateForNext.isEmpty()) {
                log.atFiner().log("No candidate found");
                return false;
            }
            next = candidateForNext.get();
            log.atFiner().log("Next expectation is %s", next);
        }
        return next.match(tracked, checker);
    }

    @Override
    public void step() {
        log.atFiner().log("Performing the step for %s", next);
        next.step();
        if (next.isFullySatisfied()) {
            log.atFiner().log("Expectation %s exhausted, clearing next expectation and removing it", next);
            expected.remove(next);
            next = null;
        } else {
            log.atFiner().log("Expectation not exhausted: %s", next);
        }
    }

    @Override
    public boolean isFullySatisfied() {
        return expected.isEmpty();
    }

    @Override
    public String describe() {
        if (next == null) {
            return "(" + expected.stream().map(Expectation::describe).collect(joining(", or ")) + ")";
        }
        return "(" + next.describe() + ", then " + expected.stream().filter(x -> x != next).map(Expectation::describe).collect(joining(", or ")) + ")";
    }

    @Override
    public String describeDirectNext() {
        if (next == null) {
            return expected.stream().map(Expectation::describeDirectNext).collect(joining(", or"));
        }
        return next.describeDirectNext();
    }
}
