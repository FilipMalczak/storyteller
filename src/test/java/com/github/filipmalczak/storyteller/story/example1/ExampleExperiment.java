package com.github.filipmalczak.storyteller.story.example1;

import com.github.filipmalczak.storyteller.api.visualize.ReportOptions;
import com.github.filipmalczak.storyteller.impl.story.*;
import com.github.filipmalczak.storyteller.impl.visualize.NitriteReportGenerator;
import com.github.filipmalczak.storyteller.impl.visualize.start.StartingPoints;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.IntStream;

import static com.github.filipmalczak.storyteller.impl.visualize.html.Bootstrap.badge;
import static com.github.filipmalczak.storyteller.impl.visualize.html.Html.literal;
import static com.github.filipmalczak.storyteller.impl.visualize.html.Html.sequence;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Flogger
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExampleExperiment {
    @Test
    @Order(1)
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


//                if (variable.equals("c"))
//                    throw new RuntimeException("last exception and well succeed in a moment");
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

    //fixme even if we do the ordering, it still fails when running all the tests with gradle, becsuse the db is already opened...
    @Test
    @Order(2)
    void renderReport(){
        var generator = new NitriteReportGenerator<String, SimpleDefinition, EpisodeType>(
            new File("examples/example1/index.no2")
        );
        generator.generateReport(
            new File("examples/example1/report"),
            StartingPoints.of(
                new StandardIdGeneratorFactory<>(),
                new SimpleDefinition("Finding x"),
                EpisodeType.STORY
            ),
            ReportOptions.<String, SimpleDefinition, EpisodeType>builder()
                .definitionRenderer(d -> {
                    if (d.getKey() == null)
                        return d.getName();
                    return sequence(
                        literal(d.getName()),
                        badge(d.getKey().toString(), false, "info")
                    ).renderHtml();
                })
                .build()
        );
//        var no2 = Nitrite.builder().filePath().readOnly().openOrCreate();
//        var renderer = new NitriteRenderer<>(new NitriteManagers<String, SimpleDefinition, EpisodeType>(no2));
//        renderer.renderToHtmlFile(
//            RenderDefinition.<String, SimpleDefinition, EpisodeType>builder()
//                .startingPoint(
//                    RenderDefinition.StartingPoint.of(
//                        new StandardIdGeneratorFactory<>(),
//                        new SimpleDefinition("Finding x"),
//                        EpisodeType.STORY
//                    )
//                )
//                .build()
//            ,
//            new File("examples/example1/report.html")
//        );
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
