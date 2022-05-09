package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.filipmalczak.storyteller.impl.testimpl.StringStringDoc.getOr;
import static com.github.filipmalczak.storyteller.impl.testimpl.StringStringDoc.put;

public class NoSqlTests {
    private ExecutionTracker<String> tracker;
    private static final TestTreeFactory FACTORY = new TestTreeFactory("TaskSkippingTests");

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>(String.class);
    }

    @Test
    @DisplayName("r(n(l(w:foo->a)))")
    void oneLeafSingleRun(){
        var exec = FACTORY.create("oneLeafSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(getOr(rootStorage, "foo", "-")+1);
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(getOr(rootStorage, "foo", "-")+2);
                tracker.mark(getOr(nodeStorage, "foo", "-")+3);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-")+4);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+5);
                    tracker.mark(getOr(leafStorage, "foo", "-")+6);
                    put(leafStorage, "foo", "a");
                    tracker.mark(getOr(rootStorage, "foo", "-")+7);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+8);
                    tracker.mark(getOr(leafStorage, "foo", "-")+9);
                });
                tracker.mark(getOr(rootStorage, "foo", "-")+10);
                tracker.mark(getOr(nodeStorage, "foo", "-")+11);
            });
            tracker.mark(getOr(rootStorage, "foo", "-")+12);
        });
        tracker.expect(
            "-1",
            "-2", "-3",
            "-4", "-5", "-6",
            "-7", "-8", "a9",
            "-10", "a11",
            "a12"
        );
    }

    @Test
    @DisplayName("r(n(l(w:foo->a))) x2")
    void oneLeafTwoRuns(){
        var exec = FACTORY.create("oneLeafTwoRuns");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(getOr(rootStorage, "foo", "-")+1);
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(getOr(rootStorage, "foo", "-")+2);
                tracker.mark(getOr(nodeStorage, "foo", "-")+3);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-")+4);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+5);
                    tracker.mark(getOr(leafStorage, "foo", "-")+6);
                    put(leafStorage, "foo", "a");
                    tracker.mark(getOr(rootStorage, "foo", "-")+7);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+8);
                    tracker.mark(getOr(leafStorage, "foo", "-")+9);
                });
                tracker.mark(getOr(rootStorage, "foo", "-")+10);
                tracker.mark(getOr(nodeStorage, "foo", "-")+11);
            });
            tracker.mark(getOr(rootStorage, "foo", "-")+12);
        });
        tracker.expect(
            "-1",
            "-2", "-3",
            "-4", "-5", "-6",
            "-7", "-8", "a9",
            "-10", "a11",
            "a12"
        );
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(getOr(rootStorage, "foo", "-")+1);
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(getOr(rootStorage, "foo", "-")+2);
                tracker.mark(getOr(nodeStorage, "foo", "-")+3);
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-")+4);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+5);
                    tracker.mark(getOr(leafStorage, "foo", "-")+6);
                    put(leafStorage, "foo", "a");
                    tracker.mark(getOr(rootStorage, "foo", "-")+7);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+8);
                    tracker.mark(getOr(leafStorage, "foo", "-")+9);
                });
                tracker.mark(getOr(rootStorage, "foo", "-")+10);
                tracker.mark(getOr(nodeStorage, "foo", "-")+11);
            });
            tracker.mark(getOr(rootStorage, "foo", "-")+12);
        });
        tracker.expect(
            "-1",
            "-2", "-3",
            "-10", "a11",
            "a12"
        );
    }

    @Test
    @DisplayName("r(n(l1(w:foo->a), l2(w:foo->b))")
    void twoLeavesSingleRun(){
        var exec = FACTORY.create("twoLeavesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(getOr(rootStorage, "foo", "-")+1);
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(getOr(rootStorage, "foo", "-")+2);
                tracker.mark(getOr(nodeStorage, "foo", "-")+3);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-")+4);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+5);
                    tracker.mark(getOr(leafStorage, "foo", "-")+6);
                    put(leafStorage, "foo", "a");
                    tracker.mark(getOr(rootStorage, "foo", "-")+7);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+8);
                    tracker.mark(getOr(leafStorage, "foo", "-")+9);
                });
                tracker.mark(getOr(rootStorage, "foo", "-")+10);
                tracker.mark(getOr(nodeStorage, "foo", "-")+11);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-")+12);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+13);
                    tracker.mark(getOr(leafStorage, "foo", "-")+14);
                    put(leafStorage, "foo", "b");
                    tracker.mark(getOr(rootStorage, "foo", "-")+15);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+16);
                    tracker.mark(getOr(leafStorage, "foo", "-")+17);
                });
                tracker.mark(getOr(rootStorage, "foo", "-")+18);
                tracker.mark(getOr(nodeStorage, "foo", "-")+19);
            });
            tracker.mark(getOr(rootStorage, "foo", "-")+20);
        });
        tracker.expect(
            "-1",
            "-2", "-3",
            "-4", "-5", "-6",
            "-7", "-8", "a9",
            "-10", "a11",
            "-12", "a13", "a14",
            "-15", "a16", "b17",
            "-18", "b19",
            "b20"
        );
    }

    @Test
    @DisplayName("r(n1(l1(w:foo->a)), n2(l2(w:foo->b)))")
    void twoNodesSingleRun(){
        var exec = FACTORY.create("twoNodesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            tracker.mark(getOr(rootStorage, "foo", "-")+1);
            rootExec.execute("first node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(getOr(rootStorage, "foo", "-") + 2);
                tracker.mark(getOr(nodeStorage, "foo", "-") + 3);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-") + 4);
                    tracker.mark(getOr(nodeStorage, "foo", "-") + 5);
                    tracker.mark(getOr(leafStorage, "foo", "-") + 6);
                    put(leafStorage, "foo", "a");
                    tracker.mark(getOr(rootStorage, "foo", "-") + 7);
                    tracker.mark(getOr(nodeStorage, "foo", "-") + 8);
                    tracker.mark(getOr(leafStorage, "foo", "-") + 9);
                });
                tracker.mark(getOr(rootStorage, "foo", "-") + 10);
                tracker.mark(getOr(nodeStorage, "foo", "-") + 11);
            });
            tracker.mark(getOr(rootStorage, "foo", "-") + 12);
            rootExec.execute("second node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(getOr(rootStorage, "foo", "-")+13);
                tracker.mark(getOr(nodeStorage, "foo", "-")+14);
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(getOr(rootStorage, "foo", "-")+15);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+16);
                    tracker.mark(getOr(leafStorage, "foo", "-")+17);
                    put(leafStorage, "foo", "b");
                    tracker.mark(getOr(rootStorage, "foo", "-")+18);
                    tracker.mark(getOr(nodeStorage, "foo", "-")+19);
                    tracker.mark(getOr(leafStorage, "foo", "-")+20);
                });
                tracker.mark(getOr(rootStorage, "foo", "-")+21);
                tracker.mark(getOr(nodeStorage, "foo", "-")+22);
            });
            tracker.mark(getOr(rootStorage, "foo", "-")+23);
        });
        tracker.expect(
            "-1",
            "-2", "-3",
            "-4", "-5", "-6",
            "-7", "-8", "a9",
            "-10", "a11",
            "a12",
            "a13", "a14",
            "a15", "a16", "a17",
            "a18", "a19", "b20",
            "a21", "b22",
            "b23"
        );
    }
}
