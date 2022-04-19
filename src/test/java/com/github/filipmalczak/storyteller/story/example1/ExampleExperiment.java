package com.github.filipmalczak.storyteller.story.example1;

import com.github.filipmalczak.storyteller.impl.story.NitriteBasedStorytellerFactory;
import com.github.filipmalczak.storyteller.impl.story.StackBasedStorytellerFactory;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Flogger
public class ExampleExperiment {
    @Test
//    @Disabled
    void run(){
        var storyteller = new NitriteBasedStorytellerFactory().create(Path.of("examples/example1"));
        storyteller.tell("Finding x", (a, as) -> {
            a.thread("Initialize best", (t, ts) -> {
                t.scene("Save starting point", rw ->{
                    rw.files().writer(Path.of("a"), w -> w.println(5));
                    rw.files().writer(Path.of("b"), w -> w.println(5));
                    rw.files().writer(Path.of("c"), w -> w.println(5));
                    rw.files().writer(Path.of("d"), w -> w.println(5));
                });
                t.scene("Calculate starting x", rw -> {
                    int aVal = Integer.parseInt(rw.files().readAll(Path.of("a")));
                    int bVal = Integer.parseInt(rw.files().readAll(Path.of("b")));
                    int cVal = Integer.parseInt(rw.files().readAll(Path.of("c")));
                    int dVal = Integer.parseInt(rw.files().readAll(Path.of("d")));
                    rw.files().writer(Path.of("x"), w -> w.println(calculate(aVal, bVal, cVal, dVal)));
                });
                t.scene("Calculate starting number of divisors", rw -> {
                    int xVal = Integer.parseInt(rw.files().readAll(Path.of("x")));
                    rw.documents()
                        .getRepository(Divisors.class)
                        .insert(new Divisors(xVal, numberOfDivisors(xVal)));
                });
            });
            for (var variable: asList("a", "b", "c", "d")) {
                a.<Integer, Divisors>decision("Find best "+variable, d ->
                    d
                        .domain(() -> IntStream.range(3, 16).mapToObj(i -> i))
                        .research((i, ra) ->
                            ra.thread("Look into "+variable+"=" + i, (t, ts) -> {
                                t.scene("Store "+variable, rw -> {
                                    rw.files().writer(Path.of(variable), w -> w.println(i));
                                });
                                t.scene("Recalculate x", rw -> {
                                    int aVal = Integer.parseInt(rw.files().readAll(Path.of("a")));
                                    int bVal = Integer.parseInt(rw.files().readAll(Path.of("b")));
                                    int cVal = Integer.parseInt(rw.files().readAll(Path.of("c")));
                                    int dVal = Integer.parseInt(rw.files().readAll(Path.of("d")));
                                    rw.files().writer(Path.of("x"), w -> w.println(calculate(aVal, bVal, cVal, dVal)));
                                });
                                t.scene("Calculate number of divisors", rw -> {
                                    int xVal = Integer.parseInt(rw.files().readAll(Path.of("x")));
                                    rw.documents()
                                        .getRepository(Divisors.class)
                                        .update(new Divisors(xVal, numberOfDivisors(xVal)), true);
                                });
                            })
                        )
                        .resultEvaluator(rs -> {
                            int xVal = Integer.parseInt(rs.files().readAll(Path.of("x")));
                            return rs.documents()
                                .getRepository(Divisors.class)
                                .find(eq("x", xVal))
                                .toList()
                                .get(0);
                        })
                        .scoreComparator(comparing(Divisors::getNoOfDivisors).reversed())
                );
            }
            int aVal = Integer.parseInt(as.files().readAll(Path.of("a")));
            int bVal = Integer.parseInt(as.files().readAll(Path.of("b")));
            int cVal = Integer.parseInt(as.files().readAll(Path.of("c")));
            int dVal = Integer.parseInt(as.files().readAll(Path.of("d")));
            int xVal = Integer.parseInt(as.files().readAll(Path.of("x")));
            int no = as.documents()
                .getRepository(Divisors.class)
                .find(eq("x", xVal))
                .toList()
                .get(0)
                .getNoOfDivisors();
            log.atInfo().log("%s^%s + %s^%s = %s", aVal, bVal, cVal, dVal, xVal);
            log.atInfo().log("Number of divisors: %s", no);
        });
    }

    @SneakyThrows
    static int calculate(int a, int b, int c, int d){
        //to simulate longer computations
        Thread.sleep((long) (Math.random()*5000));
        return (int) Math.pow(a, b) + (int) Math.pow(c, d);
    }

    @SneakyThrows
    static int numberOfDivisors(int x){
        //to simulate longer computations
        Thread.sleep((long) (Math.random()*5000));
        int out = 1;
        int limit = (int) Math.sqrt(x);
        for (int i=2; i<limit; ++i)
            if (x % i == 0)
                ++out;
        return out;
    }
}
