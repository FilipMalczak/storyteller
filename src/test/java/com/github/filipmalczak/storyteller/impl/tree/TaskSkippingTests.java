package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class TaskSkippingTests {
    private ExecutionTracker<Integer> tracker;
    private static final TestTreeFactory FACTORY = new TestTreeFactory("TaskSkippingTests");

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>();
    }

    @Test
    @DisplayName("r(n(l))")
    void runThreeTasks(){
        var exec = FACTORY.create("runThreeTasks");
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                nodeExec.executeOrdered("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
    }

    @Test
    @DisplayName("r(n(l)) -> r(n(l))")
    void runThreeTasksTwice(){
        var exec = FACTORY.create("runThreeTasksTwice");
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        tracker.clear();
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        tracker.clear();
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        tracker.clear();
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
        });
        tracker.expect(1, 2, 4, 5, 6, 7);
        tracker.clear();
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7);
        tracker.clear();;
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9);
        tracker.clear();;
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.executeOrdered("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9);
        tracker.clear();;
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
            rootExec.executeOrdered("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
                nodeExec.executeOrdered("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
            });
            tracker.mark(13);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        tracker.clear();
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
            rootExec.executeOrdered("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(10);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
                nodeExec.executeOrdered("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
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
        tracker.setSessions(exec.getSessions());
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
            rootExec.executeOrdered("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
                nodeExec.executeOrdered("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
            });
            tracker.mark(13);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
        tracker.clear();
        exec.executeOrdered("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            rootExec.executeOrdered("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.executeOrdered("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
                nodeExec.executeOrdered("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(5);
                });
                tracker.mark(6);
            });
            tracker.mark(7);
            rootExec.executeOrdered("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(8);
                nodeExec.executeOrdered("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(9);
                });
                tracker.mark(10);
                nodeExec.executeOrdered("leaf task in between", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(11);
                });
                tracker.mark(12);
                nodeExec.executeOrdered("fourth leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(13);
                });
                tracker.mark(14);
            });
            tracker.mark(15);
        });
        tracker.expect(1, 2, 4, 6, 7, 8, 10, 11, 12, 13, 14, 15);
    }

    /*
    FIXME critical: shrinking body! totally untested
     * Missing cases:
     * r(n(l1, l2)) -> r(nNew(l1, l2))
     * r(n(l1, l2)) -> r(nNew(l3, l4))
     * r(n(l1, l2)) -> r(n(lNew, l1, l2))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(lNew, l1, l2), n2(l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2, lNew), n2(l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2), n2(lNew, l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(nNew(l1, l2), n2(l3, l4))
     * r(n1(l1, l2), n2(l3, l4)) -> r(n1(l1, l2), nNew(l3, l4))
     *
     * and more
     *
     * also: two leafes with the same name, but different placement
     */
}