package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.TrivialIdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.TrivialTaskType;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.*;

class SimpleScenarios {

    @SneakyThrows
    private NitriteStorageConfig<String> forTest(String name){
        var dir = new File(new File("./test_data"), name).getAbsoluteFile();
        if (dir.exists())
            deleteDirectory(dir);
        dir.mkdirs();
        return new NitriteStorageConfig<String>(
            dir.toPath(),
            s -> s
        );
    }

    @Test
    void runThreeTasks(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeTasks"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
    }
}