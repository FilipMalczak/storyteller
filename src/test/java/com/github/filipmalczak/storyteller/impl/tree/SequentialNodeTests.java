package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SequentialNodeTests {
    private ExecutionTracker<Integer> tracker;
    private static final TestTreeFactory FACTORY = new TestTreeFactory("TaskSkippingTests");

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>(Integer.class);
    }

    @Test
    @DisplayName("r(n(l))")
    void runThreeTasks(){
        var exec = FACTORY.create("runThreeTasks");
        tracker.setSessions(exec.getSessions());
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
        tracker.setSessions(exec.getSessions());
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
        tracker.setSessions(exec.getSessions());
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
        tracker.setSessions(exec.getSessions());
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
    void runTwoLeavesThenInsertOneInBetween(){
        var exec = FACTORY.create("runTwoLeavesThenInsertOneInBetween");
        tracker.setSessions(exec.getSessions());
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
    void runThreeLeavesThenInsertOneBeforeThird(){
        var exec = FACTORY.create("runThreeLeavesThenInsertOneBeforeThird");
        tracker.setSessions(exec.getSessions());
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
    void runThreeLeavesThenInsertOneAfterFirst(){
        var exec = FACTORY.create("runThreeLeavesThenInsertOneAfterFirst");
        tracker.setSessions(exec.getSessions());
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
        tracker.setSessions(exec.getSessions());
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
        tracker.setSessions(exec.getSessions());
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
     * also: two leaves with the same name, but different placement r(n(l, l)); r(n1(l1, l2), n2(l1)); r(n1(l1, l2), n2(l2)); r(n1(l1, l2), n2(l1, l2))
     */

    @Test
    @DisplayName("r(n(l1, l2)) -> r(n(l1))")
    //todo r(n1(l1, l2), n2(l3)) -> r(n1(l2), n2(l3))
    //todo r(n1(l1), n2(l2, l3)) -> r(n1(l1), n2(l2))
    //todo r(n1(l1), n2(l2, l3)) -> r(n1(l1), n2(l3))
    void runTwoLeavesThenRemoveLatter(){
        var exec = FACTORY.create("runTwoLeavesThenRemoveLatter");
        tracker.setSessions(exec.getSessions());
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
        tracker.clear();
        Task<String, String, TrivialTaskType>[] theNode = new Task[1];
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            theNode[0] = rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 4, 5);
        assertNotNull(theNode[0]);
        assertEquals(1, theNode[0].getSubtasks().count());
        assertEquals("first leaf task", theNode[0].getSubtasks().findFirst().get().getDefinition());
        assertEquals(1, theNode[0].getDisownedSubtasks().count());
        assertEquals("second leaf task", theNode[0].getDisownedSubtasks().findFirst().get().getDefinition());
    }

    @Test
    @DisplayName("r(n(l1, l2)) -> r(n(l2))")
    void runTwoLeavesThenRemoveFormer(){
        var exec = FACTORY.create("runTwoLeavesThenRemoveFormer");
        tracker.setSessions(exec.getSessions());
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
        tracker.clear();
        Task<String, String, TrivialTaskType>[] theNode = new Task[1];
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            theNode[0] = rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
        });
        tracker.expect(1, 2, 3, 4, 5);
        assertNotNull(theNode[0]);
        assertEquals(1, theNode[0].getSubtasks().count());
        assertEquals("second leaf task", theNode[0].getSubtasks().findFirst().get().getDefinition());
        assertEquals(2, theNode[0].getDisownedSubtasks().count());
        assertEquals(
            asList(
                "first leaf task",
                "second leaf task"
            ),
            theNode[0]
                .getDisownedSubtasks()
                .map(Task::getDefinition)
                .toList()
        );
    }

    @Test
    @DisplayName("r(n1(l1, l2), n2(l3)) -> r(n1(l1), n2(l3))")
    void runTwoAndOneLeavesThenRemoveSecond(){
        var exec = FACTORY.create("runTwoAndOneLeavesThenRemoveSecond");
        tracker.setSessions(exec.getSessions());
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
            });
            tracker.mark(11);
        });
        tracker.expect(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        tracker.clear();
        Task<String, String, TrivialTaskType>[] firstNode = new Task[1];
        Task<String, String, TrivialTaskType>[] root = new Task[1];
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(1);
            firstNode[0] = rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(2);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(3);
                });
                tracker.mark(4);
            });
            tracker.mark(5);
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(6);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(7);
                });
                tracker.mark(8);
            });
            tracker.mark(9);
        });
        tracker.expect(1, 2, 4, 5, 6, 7, 8, 9);
        assertNotNull(firstNode[0]);
        assertEquals(1, firstNode[0].getSubtasks().count());
        assertEquals("first leaf task", firstNode[0].getSubtasks().findFirst().get().getDefinition());
        assertEquals(1, firstNode[0].getDisownedSubtasks().count());
        assertEquals("second leaf task", firstNode[0].getDisownedSubtasks().findFirst().get().getDefinition());
        //todo add checks for root and its disowned task
    }
}