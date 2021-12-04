package com.github.filipmalczak.storyteller.api.story;

import com.github.filipmalczak.storyteller.api.storage.Storage;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface DecisionClosure<Key> {
    void domain(Supplier<Stream<Key>> domain);
    void research(ResearchBody<Key, ArcClosure> closure);
    void criteria(CriteriaDefinition closure);

    interface CriteriaDefinition<Eval> {
        Function<Storage, Eval> evaluator();
        Comparator<Eval> comparatorForMaximization();
    }
}
