package com.github.filipmalczak.storyteller.utils;

import com.github.filipmalczak.storyteller.api.session.Sessions;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.flogger.Flogger;

import java.security.PublicKey;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class ExecutionTracker<T> {
    @NonNull Class<T> type;
    List<T> data = new ArrayList<>();
    @Getter @Setter
    @NonFinal Sessions sessions;

    public ExecutionTracker(@NonNull Class<T> type) {
        this.type = type;
    }

    private static record UnorderedGroup<T>(
        Set<T> elements
    ) {
        public UnorderedGroup<T> match(T val){
            assertThat("Next element must be one of "+elements+"; was "+val+" instead", elements.contains(val));
            Set<T> reduced = new HashSet<>(elements);
            reduced.remove(val);
            return new UnorderedGroup<>(reduced);
        }

        public boolean isEmpty(){
            return elements.isEmpty();
        }
    }

    public static <T> UnorderedGroup<T> unordered(T... vals){
        return new UnorderedGroup<>(new HashSet<>(asList(vals)));
    }

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

    private List<UnorderedGroup<T>> expectations(Object... events){
        List<UnorderedGroup<T>> out = new ArrayList<>();
        for (var event: events){
            if (event instanceof UnorderedGroup)
                out.add((UnorderedGroup<T>) event);
            else {
                require(type.isInstance(event), "Event must be either an unordered group, or explicitly %s", type);
                out.add(unordered((T) event));
            }
        }
        return out;
    }

    public void expect(Object... events){
        log.atInfo().log("tracker.expect(%s)", asList(events));
        var gathered = new LinkedList<>(data);
        for (var expectation: expectations(events)){
            while (!expectation.isEmpty()){
                expectation = expectation.match(gathered.removeFirst());
            }
        }
    }
}
