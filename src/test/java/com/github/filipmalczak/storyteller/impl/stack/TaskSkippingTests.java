package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialIdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.apache.commons.io.FileUtils.deleteDirectory;

@Flogger
class TaskSkippingTests {
    private ExecutionTracker<Integer> tracker;

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>();
    }

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
    @DisplayName("r(n(l))")
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
    @DisplayName("r(n(l)) -> r(n(l))")
    void runThreeTasksTwice(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeTasksTwice"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 4, 5);
    }

    @Test
    @DisplayName("r(n(l)) -> r(n(l, lNew))")
    void runThreeThenFourTasks(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeThenFourTasks"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
        });
        tracker.expect(1, 2, 4, 5, 6, 7);
    }


    @Test
    @DisplayName("r(n(l)) -> r(n(l, lNew)) -> r(n(l, lNew))")
    void runThreeTasksOnceThenFourTasksTwice(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeTasksOnceThenFourTasksTwice"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
        });
        tracker.expect(1, 2, 4, 5, 6, 7);
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
        });
        tracker.expect(1, 2, 4, 6, 7);
    }

    @Test
    @DisplayName("r(n(l1, l2)) -> r(n(l1, lNew, l2))")
    void runTwoLeafsThenInsertOneInBetween(){
        var exec = new StackedExecutorFactory().create(forTest("runTwoLeafsThenInsertOneInBetween"), new TrivialIdGeneratorFactory());
        var gathered = new ArrayList<Integer>();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7);
        tracker.clear();;
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
        });
        tracker.expect(1, 2, 4, 5, 6, 7, 8, 9);
    }

    @Test
    @DisplayName("r(n(l1, l2, l3)) -> r(n(l1, l2, lNew, l3))")
    void runThreeLeafsThenInsertOneBeforeThird(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeLeafsThenInsertOneBeforeThird"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9);
        tracker.clear();;
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.execute("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
            });
            tracker.mark(11);
        });
        tracker.expect(1, 2, 4, 6, 7, 8, 9, 10, 11);
    }

    @Test
    @DisplayName("r(n(l1, l2, l3)) -> r(n(l1, lNew, l2, l3))")
    void runThreeLeafsThenInsertOneAfterFirst(){
        var exec = new StackedExecutorFactory().create(forTest("runThreeLeafsThenInsertOneAfterFirst"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9);
        tracker.clear();;
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
            });
            tracker.mark(11);
        });
        tracker.expect(1, 2, 4, 5, 6, 7, 8, 9, 10, 11);
    }

    @Test
    @DisplayName("r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, lNew, l2), n2(l3, l4))")
    void runTwoNodesOfTwoLeavesThenInsertAfterFirstLeaf(){
        var exec = new StackedExecutorFactory().create(forTest("runTwoNodesOfTwoLeavesThenInsertAfterFirstLeaf"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
                nodeExec.execute("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
            });
            tracker.mark(13);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(10);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
                nodeExec.execute("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(13);
                });
                tracker.mark(14);
            });
            tracker.mark(15);
        });
        tracker.expect(1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    }

    @Test
    @DisplayName("r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2), n2(l3, lNew, l4))")
    void runTwoNodesOfTwoLeavesThenInsertAfterThirdLeaf(){
        var exec = new StackedExecutorFactory().create(forTest("runTwoNodesOfTwoLeavesThenInsertAfterThirdLeaf"), new TrivialIdGeneratorFactory());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
                nodeExec.execute("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
            });
            tracker.mark(13);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
                nodeExec.execute("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
                nodeExec.execute("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(13);
                });
                tracker.mark(14);
            });
            tracker.mark(15);
        });
        tracker.expect(1, 2, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);
    }

    /*
     * Missing cases:
     * r(n(l1, l2)) -> r(nNew(l1, l2))
     * r(n(l1, l2)) -> r(nNew(l3, l4))
     * r(n(l1, l2)) -> r(n(lNew, l1, l2))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(lNew, l1, l2), n2(l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2, lNew), n2(l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2), n2(lNew, l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(nNew(l1, l2), n2(l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2), nNew(l3, l4))
     * and more
     */
}