package com.github.filipmalczak.storyteller.impl.jgit;

import com.github.filipmalczak.storyteller.api.story.Storyteller;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

@FieldDefaults(makeFinal = true)
@Slf4j
class JGitStorytellerTest {
    File data = safeCreate();
    File tmp = safeCreateTmp();

    Storyteller storyteller = new JGitStorytelerFactory()
        .create(
            JGitStorytellerConfig.builder()
                .storyRoot(data)
                .tempRoot(tmp)
                .build()
        );

    @SneakyThrows
    private File safeCreate() {
        var out = new File("./test-data/origin");
        out.mkdirs();
        return out;
    }

    @SneakyThrows
    private File safeCreateTmp() {
        var out = new File("./test-data/tmp");
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
}