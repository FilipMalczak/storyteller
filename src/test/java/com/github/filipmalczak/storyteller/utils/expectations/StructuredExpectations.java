package com.github.filipmalczak.storyteller.utils.expectations;

import com.github.filipmalczak.storyteller.utils.expectations.condition.Condition;
import lombok.*;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;
import static org.valid4j.Assertive.neverGetHere;

@Value
@Builder
public class StructuredExpectations<E, T> {
    @NonNull Condition<E, T> condition;
    @NonNull Callback<T> onMatch;
    @NonNull Callback<T> onMismatch;
    @NonNull Consumer<T> onMissingInstructions;
    @NonNull Callback<Void> onLeftovers;
    OrderedGroup<E> expected = new OrderedGroup<>(new LinkedList<>());

    /**
     * Remember that if subnodes of this are non-concrete, then only one should match, or you'll get into nondeterministic
     * problems (because any of matching subnodes will be taken).
     */
    public static <E> Expectation<E> unordered(Object... vals){
        return new UnorderedGroup<E>(new HashSet<>(expectations(vals)));
    }

    public static <E> Expectation<E> ordered(Object... vals){
        return new OrderedGroup<>(new LinkedList<>(expectations(vals)));
    }

    private static <E> List<Expectation<E>> expectations(Object... events){
        List<Expectation<E>> out = new ArrayList<>();
        for (var event: events){
            if (event instanceof UnorderedGroup uG)
                out.add(uG);
            else if (event instanceof OrderedGroup oG)
                out.add(oG);
            else if (event instanceof ConcreteExpectation cE)
                out.add(cE);
            else
                out.add(new ConcreteExpectation<>((E) event));
        }
        return out;
    }

    public StructuredExpectations<E, T> expect(Object... expectations){
        this.expected.expectations.addAll(expectations(expectations));
        return this;
    }

    public void matchNext(T element){
        var ctx = new CallbackContext<>(element, condition.describe(), expected.describe(), expected.describeDirectNext());
        if (expected.isFullySatisfied())
            onMissingInstructions.accept(element);
        if (expected.match(element, condition::isSatisfied)){
            onMatch.run(ctx);
        } else {
            onMismatch.run(ctx);
        }
        expected.step();
    }

    public void end(){
        if (!expected.isFullySatisfied())
            onLeftovers.run(new CallbackContext<>(null, condition.describe(), expected.describe(), expected.describeDirectNext()));
    }

    public void match(List<T> elements){
        elements.forEach(this::matchNext);
    }

    public void matchAll(List<T> elements){
        match(elements);
        end();
    }
}
