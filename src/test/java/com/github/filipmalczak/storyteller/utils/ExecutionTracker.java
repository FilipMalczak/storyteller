package com.github.filipmalczak.storyteller.utils;

import com.github.filipmalczak.storyteller.api.session.Sessions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.flogger.Flogger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@NoArgsConstructor
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

    public void expect(T... events){
        var list = asList(events);
        log.atInfo().log("tracker.expect(%s)", list);
        assertThat(data, equalTo(list));
    }
}
