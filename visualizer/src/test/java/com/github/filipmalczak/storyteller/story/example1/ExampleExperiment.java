package com.github.filipmalczak.storyteller.story.example1;

import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.visualize.ReportOptions;
import com.github.filipmalczak.storyteller.impl.story.EpisodeType;
import com.github.filipmalczak.storyteller.impl.story.NitriteStorytellerFactory;
import com.github.filipmalczak.storyteller.impl.story.StorytellerDefinition;
import com.github.filipmalczak.storyteller.impl.story.StorytellerIdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.visualize.NitriteReportGenerator;
import com.github.filipmalczak.storyteller.impl.visualize.start.StartingPoints;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.junit.jupiter.api.*;

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
    @SneakyThrows
    void renderReport(){
        NitriteReportGenerator generator = null;
        int retried = 0;
        long sleep = 1000; //ms
        while (generator == null ) {
            try {
                generator = new NitriteReportGenerator<String, StorytellerDefinition, EpisodeType>(
                    new File("examples/example1/index.no2")
                );
            } catch (NitriteIOException e){
                if (e.getErrorMessage().getErrorCode().equals("NO2.2001") && retried < 5) {
                    //opened in another process
                    retried += 1;
                    sleep *= 2;
                    log.atInfo().withCause(e).log("Opening the DB failed at retry #%s; sleeping for %s ms", retried, sleep);
                    Thread.sleep(sleep);
                } else {
                    throw e;
                }
            }
        }
        generator.generateReport(
            new File("examples/example1/report"),
            StartingPoints.of(
                new StorytellerIdGeneratorFactory<>(),
                TaskSpec.of(new StorytellerDefinition("Finding x"), EpisodeType.STORY)
            ),
            ReportOptions.<String, StorytellerDefinition, EpisodeType>builder()
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
    }

}
