package com.github.filipmalczak.storyteller.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExecutionTracker<T> {
    List<T> data = new ArrayList<>();

    public void mark(T event){
        data.add(event);
    }

    public void clear(){
        data.clear();
    }

    public void expect(T... events){
        assertThat(data, equalTo(asList(events)));
    }
}
