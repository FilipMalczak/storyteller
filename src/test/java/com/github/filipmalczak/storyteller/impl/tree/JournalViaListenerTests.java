package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.utils.AssertiveListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType.*;
import static com.github.filipmalczak.storyteller.utils.AssertiveListener.expect;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JournalViaListenerTests {
    private static final TestTreeFactory FACTORY = new TestTreeFactory("DataConsistencyTests");



    @Test
    @DisplayName("r(n(l))")
    void singleRun(){
        var exec = FACTORY.create("singleRun");
        var listener = new AssertiveListener(
            expect(TaskStarted.class, "root task", ROOT),
            expect(SubtaskDefined.class, "root task", ROOT),
            expect(TaskStarted.class, "node task", SEQ_NODE),
            expect(SubtaskDefined.class, "node task", SEQ_NODE),
            expect(TaskStarted.class, "leaf task", LEAF),
            expect(InstructionsRan.class, "leaf task", LEAF),
            expect(TaskEnded.class, "leaf task", LEAF),
            expect(SubtaskIncorporated.class, "node task", SEQ_NODE),
            expect(BodyExecuted.class, "node task", SEQ_NODE),
            expect(TaskEnded.class, "node task", SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", ROOT),
            expect(BodyExecuted.class, "root task", ROOT),
            expect(TaskEnded.class, "root task", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    @Test
    @DisplayName("r(n(l)) x2")
    void reRunWithoutChanges(){
        var exec = FACTORY.create("reRunWithoutChanges");
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(InstructionsSkipped.class, "leaf task", LEAF),
            expect(SubtaskIncorporated.class, "node task", SEQ_NODE),
            expect(BodyExecuted.class, "node task", SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", ROOT),
            expect(BodyExecuted.class, "root task", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    @Test
    @DisplayName("r(n(l1)) -> r(n(l2))")
    void reRunWithChangedLeaf(){
        var exec = FACTORY.create("reRunWithChangedLeaf");
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(BodyChanged.class, "node task", SEQ_NODE),
            expect(SubtaskDisowned.class, "node task", SEQ_NODE),
            expect(TaskOrphaned.class, "leaf task", LEAF),
            expect(SubtaskDefined.class, "node task", SEQ_NODE),
            expect(TaskStarted.class, "another leaf task", LEAF),
            expect(InstructionsRan.class, "another leaf task", LEAF),
            expect(TaskEnded.class, "another leaf task", LEAF),
            expect(SubtaskIncorporated.class, "node task", SEQ_NODE),
            expect(BodyExecuted.class, "node task", SEQ_NODE),
            expect(TaskAmended.class, "node task", SEQ_NODE),
            expect(TaskEnded.class, "node task", SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", ROOT),
            expect(BodyExecuted.class, "root task", ROOT),
            expect(TaskAmended.class, "root task", ROOT),
            expect(TaskEnded.class, "root task", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("another leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    //todo r(n1(l)) -> r(n2(l))

    @Test
    @DisplayName("r(n(l1)) -> r(n(l1, l2))")
    void reRunWithAddedLeaf(){
        var exec = FACTORY.create("reRunWithAddedLeaf");
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(InstructionsSkipped.class, "leaf task", LEAF),
            expect(SubtaskIncorporated.class, "node task", SEQ_NODE),
            expect(BodyExtended.class, "node task", SEQ_NODE),
            expect(SubtaskDefined.class, "node task", SEQ_NODE),
            expect(TaskStarted.class, "another leaf task", LEAF),
            expect(InstructionsRan.class, "another leaf task", LEAF),
            expect(TaskEnded.class, "another leaf task", LEAF),
            expect(SubtaskIncorporated.class, "node task", SEQ_NODE),
            expect(BodyExecuted.class, "node task", SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", ROOT),
            expect(BodyExecuted.class, "root task", ROOT),
            expect(TaskAmended.class, "root task", ROOT),
            expect(TaskEnded.class, "root task", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
                nodeExec.execute("another leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    @Test
    @DisplayName("r(n(l1, l2)) -> r(n(l1))")
    void reRunWithRemovedLeaf(){
        var exec = FACTORY.create("reRunWithAddedLeaf");
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
                nodeExec.execute("another leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(InstructionsSkipped.class, "leaf task", LEAF),
            expect(SubtaskIncorporated.class, "node task", SEQ_NODE),
            expect(BodyNarrowed.class, "node task", SEQ_NODE),
            expect(SubtaskDisowned.class, "node task", SEQ_NODE),
            expect(TaskOrphaned.class, "another leaf task", LEAF),
            expect(BodyExecuted.class, "node task", SEQ_NODE),
            expect(TaskAmended.class, "node task", SEQ_NODE),
            expect(TaskEnded.class, "node task", SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", ROOT),
            expect(BodyExecuted.class, "root task", ROOT),
            expect(TaskAmended.class, "root task", ROOT),
            expect(TaskEnded.class, "root task", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    static class AnException extends RuntimeException {
        public AnException(String message) {
            super(message);
        }
    }

    @Test
    @DisplayName("r(throw)")
    void throwInRoot(){
        var exec = FACTORY.create("throwInRoot");
        var listener = new AssertiveListener(
            expect(TaskStarted.class, "root", ROOT),
            expect(ExceptionCaught.class, "root", ROOT, e ->
                e.getClassName().equals(AnException.class.getCanonicalName()) &&
                    e.getMessage().equals("abc")
            )
        );
        exec.getSessions().addListener(listener);
        assertThrows(
            AnException.class,
            () -> exec.execute("root", ROOT, (rt, rs) -> {throw new AnException("abc");}),
            "abc"
        );
        listener.end();
    }

    @Test
    @DisplayName("r(n(throw))")
    void throwInNode(){
        var exec = FACTORY.create("throwInNode");
        var listener = new AssertiveListener(
            expect(TaskStarted.class, "root", ROOT),
            expect(SubtaskDefined.class, "root", ROOT),
            expect(TaskStarted.class, "node", SEQ_NODE),
            expect(ExceptionCaught.class, "node", SEQ_NODE, e ->
                e.getClassName().equals(AnException.class.getCanonicalName()) &&
                    e.getMessage().equals("abc")
            ),
            expect(TaskInterrupted.class, "root", ROOT)
        );
        exec.getSessions().addListener(listener);
        assertThrows(
            AnException.class,
            () -> exec.execute("root", ROOT, (rt, rs) -> {
                rt.execute("node", SEQ_NODE, (nt, ns) -> {
                    throw new AnException("abc");
                });

            }),
            "abc"
        );
        listener.end();
    }

    @Test
    @DisplayName("r(n(l(throw)))")
    void throwInFirstLeaf(){
        var exec = FACTORY.create("throwInFirstLeaf");
        var listener = new AssertiveListener(
            expect(TaskStarted.class, "root", ROOT),
            expect(SubtaskDefined.class, "root", ROOT),
            expect(TaskStarted.class, "node", SEQ_NODE),
            expect(SubtaskDefined.class, "node", SEQ_NODE),
            expect(TaskStarted.class, "leaf", LEAF),
            expect(ExceptionCaught.class, "leaf", LEAF, e ->
                e.getClassName().equals(AnException.class.getCanonicalName()) &&
                    e.getMessage().equals("abc")
            ),
            expect(TaskInterrupted.class, "node", SEQ_NODE),
            expect(TaskInterrupted.class, "root", ROOT)
        );
        exec.getSessions().addListener(listener);
        assertThrows(
            AnException.class,
            () -> exec.execute("root", ROOT, (rt, rs) -> {
                rt.execute("node", SEQ_NODE, (nt, ns) -> {
                    nt.execute("leaf", LEAF, rw -> {
                        throw new AnException("abc");
                    });
                });

            }),
            "abc"
        );
        listener.end();
    }

    @Test
    @DisplayName("r(n(l1(), l2(throw)))")
    void throwInSecondLeaf(){
        var exec = FACTORY.create("throwInFirstLeaf");
        var listener = new AssertiveListener(
            expect(TaskStarted.class, "root", ROOT),
            expect(SubtaskDefined.class, "root", ROOT),
            expect(TaskStarted.class, "node", SEQ_NODE),
            expect(SubtaskDefined.class, "node", SEQ_NODE),
            expect(TaskStarted.class, "leaf1", LEAF),
            expect(InstructionsRan.class, "leaf1", LEAF),
            expect(TaskEnded.class, "leaf1", LEAF),
            expect(SubtaskIncorporated.class, "node", SEQ_NODE),
            expect(SubtaskDefined.class, "node", SEQ_NODE),
            expect(TaskStarted.class, "leaf2", LEAF),
            expect(ExceptionCaught.class, "leaf2", LEAF, e ->
                e.getClassName().equals(AnException.class.getCanonicalName()) &&
                    e.getMessage().equals("abc")
            ),
            expect(TaskInterrupted.class, "node", SEQ_NODE),
            expect(TaskInterrupted.class, "root", ROOT)
        );
        exec.getSessions().addListener(listener);
        assertThrows(
            AnException.class,
            () -> exec.execute("root", ROOT, (rt, rs) -> {
                rt.execute("node", SEQ_NODE, (nt, ns) -> {
                    nt.execute("leaf1", LEAF, rw -> {});
                    nt.execute("leaf2", LEAF, rw -> {
                        throw new AnException("abc");
                    });
                });

            }),
            "abc"
        );
        listener.end();
    }

    //todo throw in parallel node

    @Test
    @DisplayName("r(=n(>l1, >l2, l3)) -> r(=n(>l1, >l2, >l3))")
    void inflateParallelNode(){
        var exec = FACTORY.create("inflateParallelNode");
        exec.execute("root", ROOT, (rt, rs) -> {
            rt.execute("node", PAR_NODE,
                (nt, ns) -> {
                    nt.execute("l1", LEAF, rw -> {
                    });
                    nt.execute("l2", LEAF, rw -> {
                    });
                    nt.execute("l3", LEAF, rw -> {
                    });
                },
                (subtasks, insight) -> subtasks.stream().filter(t -> !t.getDefinition().equals("l3")).collect(toSet())
            );
        });
        var listener = new AssertiveListener(
            //fixme CRUCIAL
            //todo I need equivalent of unordered() from tracker!!!!!!!
            expect(InstructionsSkipped.class, "l1", LEAF),
            expect(InstructionsSkipped.class, "l2", LEAF),
            expect(InstructionsSkipped.class, "l3", LEAF),
            expect(BodyExecuted.class, "node", PAR_NODE),
            expect(ParallelNodeInflated.class, "node", PAR_NODE),
            expect(SubtaskDisowned.class, "node", PAR_NODE), //merge leaf is disowned
            expect(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            expect(SubtaskDefined.class, "node", PAR_NODE),
            expect(TaskStarted.class, "merge", LEAF),
            expect(InstructionsRan.class, "merge", LEAF),
            expect(TaskEnded.class, "merge", LEAF),
            //3 incorporations for leafs + 1 for merge leaf
            expect(SubtaskIncorporated.class, "node", PAR_NODE),
            expect(SubtaskIncorporated.class, "node", PAR_NODE),
            expect(SubtaskIncorporated.class, "node", PAR_NODE),
            expect(SubtaskIncorporated.class, "node", PAR_NODE),
            expect(ParallelNodeAugmented.class, "node", PAR_NODE),
            expect(TaskEnded.class, "node", PAR_NODE),
            expect(SubtaskIncorporated.class, "root", ROOT),
            expect(BodyExecuted.class, "root", ROOT),
            expect(TaskAmended.class, "root", ROOT),
            expect(TaskEnded.class, "root", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root", ROOT, (rt, rs) -> {
            rt.execute("node", PAR_NODE,
                (nt, ns) -> {
                    nt.execute("l1", LEAF, rw -> {
                    });
                    nt.execute("l2", LEAF, rw -> {
                    });
                    nt.execute("l3", LEAF, rw -> {
                    });
                },
                (subtasks, insight) -> subtasks
            );
        });
        listener.end();
    }
}
