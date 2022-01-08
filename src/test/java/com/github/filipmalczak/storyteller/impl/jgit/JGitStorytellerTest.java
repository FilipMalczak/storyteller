package com.github.filipmalczak.storyteller.impl.jgit;

import com.github.filipmalczak.storyteller.api.story.Storyteller;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
class JGitStorytellerTest {
    File data;
    File tmp;

    Storyteller storyteller;

    @BeforeEach
    public void setup(){
        String testId = UUID.randomUUID().toString();
        log.info("SETUP "+testId);
        data = safeCreate(testId);
        tmp = safeCreateTmp(testId);

        storyteller = new JGitStorytelerFactory()
            .create(
                JGitStorytellerConfig.builder()
                    .storyRoot(data)
                    .tempRoot(tmp)
                    .build()
            );
    }

    @SneakyThrows
    private File safeCreate(String sub) {
        var out = new File("./test-data/"+sub+"/origin");
        out.mkdirs();
        return out;
    }

    @SneakyThrows
    private File safeCreateTmp(String sub) {
        var out = new File("./test-data/"+sub+"/tmp");
        out.mkdirs();
        return out;
//        return Files.createTempDirectory("storyteller").toFile();
    }

    @Test
    public void simplestWithNoChecks(){
        log.info("Start");
        storyteller.tell("simplest", a ->
            a.thread("base-thread", t -> {
                t.scene("first-scene", s -> {
                    log.info("Before create");
                    s.documents().create(new SimplestRecord("hello"));
                    log.info("after create");
                });
                t.scene("second-scene", s -> {
                    log.info("Before create");
                    s.documents().create(new SimplestRecord("world"));
                    log.info("after create");
                });
            })
        );
        log.info("End");
    }

    static class ControlledException extends RuntimeException{}

    private static void simulateFailure(){
        log.info("PSEUDO-FAIL");
         throw new ControlledException();
    }

    @Test
    public void simplestSuccessRerunWithBasicChecks() {
        log.info("Start");
        log.info("");
        log.info("========================= RUN 0 =========================");
        log.info("");
        int[] x = new int[]{0};
        int[] y = new int[]{0};

        //if initial run was succesful
        storyteller.tell("simplest with checks", a ->
            a.thread("base-thread", t -> {
                t.scene("x", s -> {
                    s.documents().create(new SimplestRecord("hello"));
                    x[0] += 1;
                });
                t.scene("y", s -> {
                    s.documents().create(new SimplestRecord("world"));
                    y[0] += 1;
                });
            })
        );

        assertEquals(1, x[0]);
        assertEquals(1, y[0]);

        x[0] = 0;
        y[0] = 0;

        log.info("");
        log.info("========================= RUN 1 =========================");
        log.info("");

        //then rerunning should be cheap
        storyteller.tell("simplest with checks", a ->
            a.thread("base-thread", t -> {
                t.scene("x", s -> {
                    s.documents().create(new SimplestRecord("hello"));
                    x[0] += 1;
                });
                t.scene("y", s -> {
                    simulateFailure();
                    s.documents().create(new SimplestRecord("world"));
                    y[0] += 1;
                });
            })
        );

        assertEquals(0, x[0]);
        assertEquals(0, y[0]);
    }

    @Test
    public void simplestFailuresWithBasicChecks(){
        log.info("Start");
        log.info("");
        log.info("========================= RUN 0 =========================");
        log.info("");
        int[] x = new int[] { 0 };
        int[] y = new int[] { 0 };

        //we try to run, but it fails on fist scene
        try {
            storyteller.tell("simplest with checks", a ->
                a.thread("base-thread", t -> {
                    t.scene("x", s -> {
                        simulateFailure();
                        s.documents().create(new SimplestRecord("hello"));
                        x[0] += 1;
                    });
                    t.scene("y", s -> {
                        s.documents().create(new SimplestRecord("world"));
                        y[0] += 1;
                    });
                })
            );
        } catch (ControlledException e1){
        }

        assertEquals(0, x[0]);
        assertEquals(0, y[0]);

        log.info("");
        log.info("========================= RUN 1 =========================");
        log.info("");

        //we rerun, first scene works, but it fails on second scene
        try {
            storyteller.tell("simplest with checks", a ->
                a.thread("base-thread", t -> {
                    t.scene("x", s -> {
                        s.documents().create(new SimplestRecord("hello"));
                        x[0] += 1;
                    });
                    t.scene("y", s -> {
                        simulateFailure();
                        s.documents().create(new SimplestRecord("world"));
                        y[0] += 1;
                    });
                })
            );
        } catch (ControlledException e1){
        }

        assertEquals(1, x[0]);
        x[0] = 0;
        assertEquals(0, y[0]);

        log.info("");
        log.info("========================= RUN 2 =========================");
        log.info("");

        //so we retry, and it succeeds

        storyteller.tell("simplest with checks", a ->
            a.thread("base-thread", t -> {
                t.scene("x", s -> {
                    s.documents().create(new SimplestRecord("hello"));
                    x[0] += 1;
                });
                t.scene("y", s -> {
                    s.documents().create(new SimplestRecord("world"));
                    y[0] += 1;
                });
            })
        );

        assertEquals(0, x[0]);
        assertEquals(1, y[0]);
        y[0] = 0;

        log.info("");
        log.info("========================= RUN 3 =========================");
        log.info("");

        //and we get another retry for effectively free

        storyteller.tell("simplest with checks", a ->
            a.thread("base-thread", t -> {
                t.scene("x", s -> {
                    s.documents().create(new SimplestRecord("hello"));
                    x[0] += 1;
                });
                t.scene("y", s -> {
                    s.documents().create(new SimplestRecord("world"));
                    y[0] += 1;
                });
            })
        );

        assertEquals(0, x[0]);
        assertEquals(0, y[0]);

        log.info("End");
    }
}