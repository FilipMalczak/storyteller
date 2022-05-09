package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.storage.files.exceptions.UnresolvablePathException;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.github.filipmalczak.storyteller.utils.ExecutionTracker.unordered;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

public class FilesTests {
    private ExecutionTracker<String> tracker;
    private static final TestTreeFactory FACTORY = new TestTreeFactory("DataConsistencyTests");

    @BeforeEach
    private void setup(){
        tracker = new ExecutionTracker<>(String.class);
    }

    @Test
    @DisplayName("r(n(l(w:foo->a)))")
    void oneLeafNoDirectoriesSingleRun(){
        var exec = FACTORY.create("oneLeafNoDirectoriesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
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
    @DisplayName("r(n(l(w:foo->a))) x2")
    void oneLeafNoDirectoriesTwoRuns(){
        var exec = FACTORY.create("oneLeafNoDirectoriesTwoRuns");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
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

    @Test
    @DisplayName("r(n(l1(w:foo->a), l2(w:foo->b))")
    void twoLeavesNoDirectoriesSingleRun(){
        var exec = FACTORY.create("twoLeavesNoDirectoriesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+3);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("b"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+4);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+5);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+6);
        });
        tracker.expect("a1", "a2", "a3", "b4", "b5", "b6");
    }

    @Test
    @DisplayName("r(n(l1(w:foo->a), l2(w:foo->b)) x2")
    void twoLeavesNoDirectoriesTwoRuns(){
        var exec = FACTORY.create("twoLeavesNoDirectoriesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+3);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("b"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+4);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+5);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+6);
        });
        tracker.expect("a1", "a2", "a3", "b4", "b5", "b6");
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+3);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("b"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+4);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+5);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+6);
        });
        tracker.expect("a2", "b5", "b6");
    }

    @Test
    @DisplayName("r(n1(l1(w:foo->a, bar->x), l2(w:foo->b), n2(l3(w:foo->c)))")
    void twoLeavesThenOneLeafNoDirectoriesSingleRun(){
        var exec = FACTORY.create("twoLeavesThenOneLeafNoDirectoriesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("first node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    leafStorage.files().writer(Path.of("bar.txt"), w -> w.println("x"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+3);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+3);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("b"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+4);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+4);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+5);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+5);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+6);
            tracker.mark(rootStorage.files().readAll(Path.of("bar.txt"))+6);
            rootExec.execute("second node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+7);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+7);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+8);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+8);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("c"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+9);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+9);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+10);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+10);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+11);
            tracker.mark(rootStorage.files().readAll(Path.of("bar.txt"))+11);
        });
        tracker.expect(
            "a1", "x1",
            "a2", "x2",
            "a3", "x3",
            "b4", "x4",
            "b5", "x5",
            "b6", "x6",
            "b7", "x7",
            "b8", "x8",
            "c9", "x9",
            "c10", "x10",
            "c11", "x11"
        );
    }

    @Test
    @DisplayName("r(n1(l1(w:foo->a, bar->x), l2(w:foo->b), n2(l3(w:foo->c)))")
    void twoLeavesThenOneLeafNoDirectoriesTwoRuns(){
        var exec = FACTORY.create("twoLeavesThenOneLeafNoDirectoriesTwoRuns");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("first node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    leafStorage.files().writer(Path.of("bar.txt"), w -> w.println("x"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+3);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+3);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("b"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+4);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+4);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+5);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+5);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+6);
            tracker.mark(rootStorage.files().readAll(Path.of("bar.txt"))+6);
            rootExec.execute("second node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+7);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+7);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+8);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+8);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("c"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+9);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+9);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+10);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+10);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+11);
            tracker.mark(rootStorage.files().readAll(Path.of("bar.txt"))+11);
        });
        tracker.expect(
            "a1", "x1",
            "a2", "x2",
            "a3", "x3",
            "b4", "x4",
            "b5", "x5",
            "b6", "x6",
            "b7", "x7",
            "b8", "x8",
            "c9", "x9",
            "c10", "x10",
            "c11", "x11"
        );
        tracker.clear();
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("first node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("first leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                    leafStorage.files().writer(Path.of("bar.txt"), w -> w.println("x"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+1);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+1);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+2);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+2);
                nodeExec.execute("second leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+3);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+3);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("b"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+4);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+4);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+5);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+5);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+6);
            tracker.mark(rootStorage.files().readAll(Path.of("bar.txt"))+6);
            rootExec.execute("second node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+7);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+7);
                nodeExec.execute("third leaf task", TrivialTaskType.LEAF, (leafStorage) -> {
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+8);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+8);
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("c"));
                    tracker.mark(leafStorage.files().readAll(Path.of("foo.txt"))+9);
                    tracker.mark(leafStorage.files().readAll(Path.of("bar.txt"))+9);
                });
                tracker.mark(nodeStorage.files().readAll(Path.of("foo.txt"))+10);
                tracker.mark(nodeStorage.files().readAll(Path.of("bar.txt"))+10);
            });
            tracker.mark(rootStorage.files().readAll(Path.of("foo.txt"))+11);
            tracker.mark(rootStorage.files().readAll(Path.of("bar.txt"))+11);
        });
        tracker.expect(
            "a2", "x2",
            "b5", "x5",
            "b6", "x6",
            "b7", "x7",
            "c10", "x10",
            "c11", "x11"
        );
    }

    @Test
    @DisplayName("r(read)")
    void testNotCreated(){
        var exec = FACTORY.create("testNotCreated");
        tracker.setSessions(exec.getSessions());
        exec.execute("ROOT", TrivialTaskType.ROOT, (t, s) -> {
            tracker.mark("root");
            assertFalse(s.files().exists(Path.of("nonexistent.txt")));
            assertFalse(s.files().exists(Path.of("dir/nonexistent.txt")));
            assertThrows(
                UnresolvablePathException.class,
                () -> s.files().read(Path.of("nonexistent.txt"), is -> {}),
                "Path 'nonexistent.txt' cannot be resolved! Referenced file has either been deleted or never existed!"
            );
            assertThrows(
                UnresolvablePathException.class,
                () -> s.files().read(Path.of("dir/nonexistent.txt"), is -> {}),
                "Path 'dir/nonexistent.txt' cannot be resolved! Referenced file has either been deleted or never existed!"
            );
        });
        //keep this just to make sure that we're not actually omitting asserts
        tracker.expect("root");
    }

    @Test
    @DisplayName("r(l1(write), l2(delete))")
    void testWriteAndDeleteInDifferentLeaves(){
        var exec = FACTORY.create("testWriteAndDeleteInDifferentLeaves");
        tracker.setSessions(exec.getSessions());
        exec.execute("ROOT", TrivialTaskType.ROOT, (t, s) -> {
            tracker.mark("root");
            assertFalse(s.files().exists(Path.of("nonexistent.txt")));
            assertFalse(s.files().exists(Path.of("dir/nonexistent.txt")));
            t.execute("leaf1", TrivialTaskType.LEAF, rw -> {
                tracker.mark("leaf1");
                assertFalse(rw.files().exists(Path.of("nonexistent.txt")));
                assertFalse(rw.files().exists(Path.of("dir/nonexistent.txt")));
                rw.files().writer(Path.of("nonexistent.txt"), w -> w.println("x"));
                rw.files().writer(Path.of("dir/nonexistent.txt"), w -> w.println("y"));
                assertTrue(rw.files().exists(Path.of("nonexistent.txt")));
                assertTrue(rw.files().exists(Path.of("dir/nonexistent.txt")));
            });
            assertEquals("x", s.files().readAll(Path.of("nonexistent.txt")));
            assertEquals("y", s.files().readAll(Path.of("dir/nonexistent.txt")));
            t.execute("leaf2", TrivialTaskType.LEAF, rw -> {
                tracker.mark("leaf2");
                assertTrue(rw.files().exists(Path.of("nonexistent.txt")));
                assertTrue(rw.files().exists(Path.of("dir/nonexistent.txt")));
                rw.files().delete(Path.of("nonexistent.txt"));
                rw.files().delete(Path.of("dir/nonexistent.txt"));
                assertFalse(rw.files().exists(Path.of("nonexistent.txt")));
                assertFalse(rw.files().exists(Path.of("dir/nonexistent.txt")));
                assertTrue(s.files().exists(Path.of("nonexistent.txt")));
                assertTrue(s.files().exists(Path.of("dir/nonexistent.txt")));
                assertThrows(
                    UnresolvablePathException.class,
                    () -> rw.files().read(Path.of("nonexistent.txt"), is -> {}),
                    "Path 'nonexistent.txt' cannot be resolved! Referenced file has either been deleted or never existed!"
                );
                assertThrows(
                    UnresolvablePathException.class,
                    () -> rw.files().read(Path.of("dir/nonexistent.txt"), is -> {}),
                    "Path 'dir/nonexistent.txt' cannot be resolved! Referenced file has either been deleted or never existed!"
                );
            });
            assertFalse(s.files().exists(Path.of("nonexistent.txt")));
            assertFalse(s.files().exists(Path.of("dir/nonexistent.txt")));
            assertThrows(
                UnresolvablePathException.class,
                () -> s.files().read(Path.of("nonexistent.txt"), is -> {}),
                "Path 'nonexistent.txt' cannot be resolved! Referenced file has either been deleted or never existed!"
            );
            assertThrows(
                UnresolvablePathException.class,
                () -> s.files().read(Path.of("dir/nonexistent.txt"), is -> {}),
                "Path 'dir/nonexistent.txt' cannot be resolved! Referenced file has either been deleted or never existed!"
            );
        });
        //keep this just to make sure that we're not actually omitting asserts
        tracker.expect("root", "leaf1", "leaf2");
    }

    //todo r(n(write, delete))

    @Test
    @DisplayName("r(l(write, delete, write))")
    void testWriteDeleteAndWriteAgainInSingleLeaf(){
        var exec = FACTORY.create("testWriteAndDeleteInDifferentLeaves");
        tracker.setSessions(exec.getSessions());
        exec.execute("ROOT", TrivialTaskType.ROOT, (t, s) -> {
            tracker.mark("root");
            assertFalse(s.files().exists(Path.of("nonexistent.txt")));
            assertFalse(s.files().exists(Path.of("dir/nonexistent.txt")));
            t.execute("leaf", TrivialTaskType.LEAF, rw -> {
                tracker.mark("leaf");
                assertFalse(rw.files().exists(Path.of("nonexistent.txt")));
                assertFalse(rw.files().exists(Path.of("dir/nonexistent.txt")));
                rw.files().writer(Path.of("nonexistent.txt"), w -> w.println("x"));
                rw.files().writer(Path.of("dir/nonexistent.txt"), w -> w.println("y"));
                assertTrue(rw.files().exists(Path.of("nonexistent.txt")));
                assertTrue(rw.files().exists(Path.of("dir/nonexistent.txt")));
                rw.files().delete(Path.of("nonexistent.txt"));
                rw.files().delete(Path.of("dir/nonexistent.txt"));
                assertFalse(rw.files().exists(Path.of("nonexistent.txt")));
                assertFalse(rw.files().exists(Path.of("dir/nonexistent.txt")));
                rw.files().writer(Path.of("nonexistent.txt"), w -> w.println("a"));
                rw.files().writer(Path.of("dir/nonexistent.txt"), w -> w.println("b"));
                assertTrue(rw.files().exists(Path.of("nonexistent.txt")));
                assertTrue(rw.files().exists(Path.of("dir/nonexistent.txt")));
                assertEquals("a", rw.files().readAll(Path.of("nonexistent.txt")));
                assertEquals("b", rw.files().readAll(Path.of("dir/nonexistent.txt")));
            });
            assertTrue(s.files().exists(Path.of("nonexistent.txt")));
            assertTrue(s.files().exists(Path.of("dir/nonexistent.txt")));
            assertEquals("a", s.files().readAll(Path.of("nonexistent.txt")));
            assertEquals("b", s.files().readAll(Path.of("dir/nonexistent.txt")));
        });
    }

    //todo r(l1(write, delete), l2(write))
    //todo r(l1(write), l2(delete, write))
    //todo r(l1(write), l2(delete), l3(write))
    //todo r(l1(write), l2(delete), l3(write), l4(delete))

    @Test
    @DisplayName("r(-n1(l1(w: foo=1, bar=2, baz=3)), =n2(>l1(w:foo=4), >l3(w:bar=5), l4(w:baz=6)))")
    void writeInSequentialThenWriteInParallel(){
        var exec = FACTORY.create("writeInSequentialThenWriteInParallel");
        tracker.setSessions(exec.getSessions());
        exec.execute("ROOT", TrivialTaskType.ROOT, (t, rs) -> {
            tracker.mark("root");
            t.execute("sequential", TrivialTaskType.SEQ_NODE, (n, ns) -> {
                tracker.mark("seq");
                t.execute("l1", TrivialTaskType.LEAF, rw -> {
                    tracker.mark("l1");
                    rw.files().writer(Path.of("foo"), w -> w.println(1));
                    rw.files().writer(Path.of("bar"), w -> w.println(2));
                    rw.files().writer(Path.of("baz"), w -> w.println(3));
                    assertEquals("1", rw.files().readAll(Path.of("foo")));
                    assertEquals("2", rw.files().readAll(Path.of("bar")));
                    assertEquals("3", rw.files().readAll(Path.of("baz")));
                });
            });

            assertEquals("1", rs.files().readAll(Path.of("foo")));
            assertEquals("2", rs.files().readAll(Path.of("bar")));
            assertEquals("3", rs.files().readAll(Path.of("baz")));
            t.execute("parallel", TrivialTaskType.PAR_NODE,
                (n, ns) -> {
                    tracker.mark("par");
                    assertEquals("1", ns.files().readAll(Path.of("foo")));
                    assertEquals("2", ns.files().readAll(Path.of("bar")));
                    assertEquals("3", ns.files().readAll(Path.of("baz")));
                    n.execute("l2", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l2");
                        rw.files().writer(Path.of("foo"), w -> w.println(4));
                        assertEquals("4", rw.files().readAll(Path.of("foo")));
                        assertEquals("2", rw.files().readAll(Path.of("bar")));
                        assertEquals("3", rw.files().readAll(Path.of("baz")));
                    });
                    n.execute("l3", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l3");
                        rw.files().writer(Path.of("bar"), w -> w.println(5));
                        assertEquals("1", rw.files().readAll(Path.of("foo")));
                        assertEquals("5", rw.files().readAll(Path.of("bar")));
                        assertEquals("3", rw.files().readAll(Path.of("baz")));
                    });
                    n.execute("l4", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l4");
                        rw.files().writer(Path.of("baz"), w -> w.println(6));
                        assertEquals("1", rw.files().readAll(Path.of("foo")));
                        assertEquals("2", rw.files().readAll(Path.of("bar")));
                        assertEquals("6", rw.files().readAll(Path.of("baz")));
                    });

                    assertEquals("1", ns.files().readAll(Path.of("foo")));
                    assertEquals("2", ns.files().readAll(Path.of("bar")));
                    assertEquals("3", ns.files().readAll(Path.of("baz")));
                },
                (ids, insight) -> ids.stream().filter(task -> !task.getDefinition().equals("l4")).collect(toSet())
            );
            assertEquals("4", rs.files().readAll(Path.of("foo")));
            assertEquals("5", rs.files().readAll(Path.of("bar")));
            assertEquals("3", rs.files().readAll(Path.of("baz")));
        });
        tracker.expect("root", "seq", "l1", "par", unordered("l2", "l3", "l4"));
    }

    @Test
    @DisplayName("r(=n1(>l1(w: foo=1), >l2(w: bar=2), l3(baz=3)), =n2(l4(w:foo=4), >l5(w:bar=5), >l6(w:baz=6)))")
    void writeInTwoParallels(){
        var exec = FACTORY.create("writeInTwoParallels");
        tracker.setSessions(exec.getSessions());
        exec.execute("ROOT", TrivialTaskType.ROOT, (t, rs) -> {
            tracker.mark("root");
            t.execute("par1", TrivialTaskType.PAR_NODE,
                (n, ns) -> {
                    tracker.mark("par1");
                    n.execute("l1", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l1");
                        rw.files().writer(Path.of("foo"), w -> w.println(1));
                        assertEquals("1", rw.files().readAll(Path.of("foo")));
                        assertFalse(rw.files().exists(Path.of("bar")));
                        assertFalse(rw.files().exists(Path.of("baz")));
                    });
                    n.execute("l2", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l2");
                        rw.files().writer(Path.of("bar"), w -> w.println(2));
                        assertEquals("2", rw.files().readAll(Path.of("bar")));
                        assertFalse(rw.files().exists(Path.of("foo")));
                        assertFalse(rw.files().exists(Path.of("baz")));
                    });
                    n.execute("l3", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l3");
                        rw.files().writer(Path.of("baz"), w -> w.println(3));
                        assertEquals("3", rw.files().readAll(Path.of("baz")));
                        assertFalse(rw.files().exists(Path.of("foo")));
                        assertFalse(rw.files().exists(Path.of("bar")));
                    });
                },
                (ids, insight) -> ids.stream().filter(task -> !task.getDefinition().equals("l3")).collect(toSet())
            );
            assertEquals("1", rs.files().readAll(Path.of("foo")));
            assertEquals("2", rs.files().readAll(Path.of("bar")));
            assertFalse(rs.files().exists(Path.of("baz")));
            t.execute("par2", TrivialTaskType.PAR_NODE,
                (n, ns) -> {
                    tracker.mark("par2");
                    n.execute("l4", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l4");
                        rw.files().writer(Path.of("foo"), w -> w.println(4));
                        assertEquals("4", rw.files().readAll(Path.of("foo")));
                        assertEquals("2", rw.files().readAll(Path.of("bar")));
                        assertFalse(rw.files().exists(Path.of("baz")));
                    });
                    n.execute("l5", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l5");
                        rw.files().writer(Path.of("bar"), w -> w.println(5));
                        assertEquals("1", rw.files().readAll(Path.of("foo")));
                        assertEquals("5", rw.files().readAll(Path.of("bar")));
                        assertFalse(rw.files().exists(Path.of("baz")));
                    });
                    n.execute("l6", TrivialTaskType.LEAF, rw -> {
                        tracker.mark("l6");
                        rw.files().writer(Path.of("baz"), w -> w.println(6));
                        assertEquals("1", rw.files().readAll(Path.of("foo")));
                        assertEquals("2", rw.files().readAll(Path.of("bar")));
                        assertEquals("6", rw.files().readAll(Path.of("baz")));
                    });
                },
                (ids, insight) -> ids.stream().filter(task -> !task.getDefinition().equals("l4")).collect(toSet())
            );
            assertEquals("1", rs.files().readAll(Path.of("foo")));
            assertEquals("5", rs.files().readAll(Path.of("bar")));
            assertEquals("6", rs.files().readAll(Path.of("baz")));
        });
        tracker.expect("root", "par1", unordered("l1", "l2", "l3"), "par2", unordered("l4", "l5", "l6"));
    }
}
