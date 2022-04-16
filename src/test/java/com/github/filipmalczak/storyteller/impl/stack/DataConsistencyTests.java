package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialIdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.apache.commons.io.FileUtils.deleteDirectory;

public class DataConsistencyTests {
    //todo extract abstract test
    private ExecutionTracker<String> tracker;

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>();
    }

    @SneakyThrows
    private NitriteStorageConfig<String> forTest(String name){
        var dir = new File(new File("./test_data/data_consistency"), name).getAbsoluteFile();
        if (dir.exists())
            deleteDirectory(dir);
        dir.mkdirs();
        return new NitriteStorageConfig<String>(
            dir.toPath(),
            s -> s
        );
    }

    @Test
    void oneLeafNoDirectoriesSingleRun(){
        var exec = new StackedExecutorFactory().create(forTest("oneLeafNoDirectoriesSingleRun"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+3);
        });
        tracker.expect("a1", "a2", "a3");
    }

    @Test
    void oneLeafNoDirectoriesTwoRuns(){
        var exec = new StackedExecutorFactory().create(forTest("oneLeafNoDirectoriesTwoRuns"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+3);
        });
        tracker.expect("a1", "a2", "a3");
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+3);
        });
        tracker.expect("a2", "a3");
    }
}
