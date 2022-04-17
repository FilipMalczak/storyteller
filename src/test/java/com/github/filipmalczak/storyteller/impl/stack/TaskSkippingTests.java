package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.testimpl.TestStackFactory;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialIdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

import static org.apache.commons.io.FileUtils.deleteDirectory;

class TaskSkippingTests {
    private ExecutionTracker<Integer> tracker;
    private static final TestStackFactory FACTORY = new TestStackFactory("TaskSkippingTests");

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>();
    }

    @Test
    @DisplayName("r(n(l))")
    void runThreeTasks(){
        var exec = FACTORY.create("runThreeTasks");
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
        var exec = FACTORY.create("runThreeTasksTwice");
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
        var exec = FACTORY.create("runThreeThenFourTasks");
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
        var exec = FACTORY.create("runThreeTasksOnceThenFourTasksTwice");
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
        var exec = FACTORY.create("runTwoLeafsThenInsertOneInBetween");
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
        var exec = FACTORY.create("runThreeLeafsThenInsertOneBeforeThird");
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
        var exec = FACTORY.create("runThreeLeafsThenInsertOneAfterFirst");
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
        var exec = FACTORY.create("runTwoNodesOfTwoLeavesThenInsertAfterFirstLeaf");
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
        var exec = FACTORY.create("runTwoNodesOfTwoLeavesThenInsertAfterThirdLeaf");
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