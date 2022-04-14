package com.github.filipmalczak.storyteller.impl;

import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public class IterationUtils {
    public static <T> Stream<T> toStream(Iterable<T> iterable){
        return stream(iterable.spliterator(), false);
    }
}
