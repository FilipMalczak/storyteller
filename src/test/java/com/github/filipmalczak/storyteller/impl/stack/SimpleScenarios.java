package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.TrivialIdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.TrivialTaskType;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@Flogger
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

    @Test
    void runThreeTasksTwice(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeTasksTwice"), new TrivialIdGeneratorFactory());
        var gathered = new ArrayList<Integer>();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
            });
            gathered.add(5);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 3, 4, 5)));
        gathered.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
            });
            gathered.add(5);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 4, 5)));
    }

    @Test
    void runThreeThenFourTasks(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeThenFourTasks"), new TrivialIdGeneratorFactory());
        var gathered = new ArrayList<Integer>();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
            });
            gathered.add(5);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 3, 4, 5)));
        gathered.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(5);
                });
                gathered.add(6);
            });
            gathered.add(7);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 4, 5, 6, 7)));
    }


    @Test
    void runThreeTasksOnceThenFourTasksTwice(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeTasksOnceThenFourTasksTwice"), new TrivialIdGeneratorFactory());
        var gathered = new ArrayList<Integer>();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
            });
            gathered.add(5);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 3, 4, 5)));
        gathered.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(5);
                });
                gathered.add(6);
            });
            gathered.add(7);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 4, 5, 6, 7)));gathered.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(5);
                });
                gathered.add(6);
            });
            gathered.add(7);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 4, 6, 7)));
    }

    @Test
    @Disabled
    void runTwoLeafsThenInsertOneInBetween(){
        var exec = new StackedExecutorFactory().create(forTest("runTwoLeafsThenInsertOneInBetween"), new TrivialIdGeneratorFactory());
        var gathered = new ArrayList<Integer>();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(5);
                });
                gathered.add(6);
            });
            gathered.add(7);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 3, 4, 5, 6, 7)));
        gathered.clear();;
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            gathered.add(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                gathered.add(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(3);
                });
                gathered.add(4);
                nodeExec.execute("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(5);
                });
                gathered.add(6);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    gathered.add(7);
                });
                gathered.add(8);
            });
            gathered.add(9);
        });
        assertThat(gathered, equalTo(List.of(1, 2, 4, 5, 6, 7, 8, 9)));
    }
}