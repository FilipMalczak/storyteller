package com.github.filipmalczak.storyteller.api.story.closure;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.story.body.ResearchBody;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


//todo
public interface DecisionClosure<Key> {
    void domain(Supplier<Stream<Key>> domain);
    void research(ResearchBody<Key, ArcClosure> closure);
    void criteria(CriteriaDefinition closure);

    interface CriteriaDefinition<Eval> {
        Function<ReadStorage, Eval> evaluator();
        Comparator<Eval> comparatorForMaximization();
    }
}
