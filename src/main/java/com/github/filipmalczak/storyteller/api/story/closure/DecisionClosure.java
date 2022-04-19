package com.github.filipmalczak.storyteller.api.story.closure;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.story.body.ResearchBody;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


//todo
public interface DecisionClosure<Key, Score, NoSql> {
    DecisionClosure<Key, Score, NoSql> domain(Supplier<Stream<Key>> domain);
    DecisionClosure<Key, Score, NoSql> research(ResearchBody<Key, ArcClosure<NoSql>> closure);
    DecisionClosure<Key, Score, NoSql> resultEvaluator(Function<ReadStorage<NoSql>, Score> eval);
    DecisionClosure<Key, Score, NoSql> scoreComparator(Comparator<Score> comparator);
}
