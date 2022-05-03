package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.storage.files.exceptions.UnresolvablePathException;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.ExecutionTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

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
    @DisplayName("r(n(l(w:foo->a))) x2")
    void oneLeafNoDirectoriesTwoRuns(){
        var exec = FACTORY.create("oneLeafNoDirectoriesTwoRuns");
        tracker.setSessions(exec.getSessions());
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

    @Test
    @DisplayName("r(n(l1(w:foo->a), l2(w:foo->b))")
    void twoLeavesNoDirectoriesSingleRun(){
        var exec = FACTORY.create("twoLeavesNoDirectoriesSingleRun");
        tracker.setSessions(exec.getSessions());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("first node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
            rootExec.execute("second node task", TrivialTaskType.NODE, (nodeExec, nodeStorage) -> {
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
    @DisplayName("r(n1(write), n2(delete))")
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
    @DisplayName("r(n(write, delete, write))")
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

    //todo r(n1(write, delete), n2(write))
    //todo r(n1(write), n2(delete, write))
    //todo r(n1(write), n2(delete), n3(write))
    //todo r(n1(write), n2(delete), n3(write), n4(delete))
}
