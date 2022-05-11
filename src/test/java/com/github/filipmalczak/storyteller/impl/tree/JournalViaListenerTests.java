package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.session.listener.LoggingJournalListener;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.AssertiveListener;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory.matchesSubtask;
import static com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType.*;
import static com.github.filipmalczak.storyteller.utils.AssertiveListener.entryForTask;
import static com.github.filipmalczak.storyteller.utils.expectations.StructuredExpectations.ordered;
import static com.github.filipmalczak.storyteller.utils.expectations.StructuredExpectations.unordered;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Flogger
public class JournalViaListenerTests {
    private static final TestTreeFactory FACTORY = new TestTreeFactory("DataConsistencyTests");

    private static TaskTree.IncorporationFilter<String, String, TrivialTaskType, Nitrite> acceptAll(){
        return (subtasks, i) -> subtasks;
    }

    private static TaskTree.IncorporationFilter<String, String, TrivialTaskType, Nitrite> accept(String... definitions){
        return (subtasks, insights) -> subtasks.stream().filter(x -> Stream.of(definitions).anyMatch(x.getDefinition()::equals)).collect(toSet());
    }

    private static TaskTree.IncorporationFilter<String, String, TrivialTaskType, Nitrite> discard(String... definitions){
        return (subtasks, insights) -> subtasks.stream().filter(x -> Stream.of(definitions).noneMatch(x.getDefinition()::equals)).collect(toSet());
    }

    //todo add predicates to all the defined/incorporated/disowned that check if id matches expected subtask def/type

    @Test
    @DisplayName("r(n(l))")
    void singleRun(){
        var exec = FACTORY.create("singleRun");
        var listener = new AssertiveListener(
            entryForTask(TaskStarted.class, "root task", ROOT),
            entryForTask(SubtaskDefined.class, "root task", ROOT, matchesSubtask("node task", SEQ_NODE)),
            entryForTask(TaskStarted.class, "node task", SEQ_NODE),
            entryForTask(SubtaskDefined.class, "node task", SEQ_NODE, matchesSubtask("leaf task", LEAF)),
            entryForTask(TaskStarted.class, "leaf task", LEAF),
            entryForTask(InstructionsRan.class, "leaf task", LEAF),
            entryForTask(TaskEnded.class, "leaf task", LEAF),
            entryForTask(SubtaskIncorporated.class, "node task", SEQ_NODE, matchesSubtask("leaf task", LEAF)),
            entryForTask(BodyExecuted.class, "node task", SEQ_NODE),
            entryForTask(TaskEnded.class, "node task", SEQ_NODE),
            entryForTask(SubtaskIncorporated.class, "root task", ROOT, matchesSubtask("node task", SEQ_NODE)),
            entryForTask(BodyExecuted.class, "root task", ROOT),
            entryForTask(TaskEnded.class, "root task", ROOT)
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
    @DisplayName("r(=n(>l1, >l2, l3))")
    void singleRunForParallelNode(){
        var exec = FACTORY.create("singleRunForParallelNode");
        var listener = new AssertiveListener(
            entryForTask(TaskStarted.class, "root task", ROOT),
            entryForTask(SubtaskDefined.class, "root task", ROOT, matchesSubtask("node task", PAR_NODE)),
            entryForTask(TaskStarted.class, "node task", PAR_NODE),
            unordered(
                ordered(
                    entryForTask(SubtaskDefined.class, "node task", PAR_NODE, matchesSubtask("leaf1", LEAF)),
                    entryForTask(TaskStarted.class, "leaf1", LEAF),
                    entryForTask(InstructionsRan.class, "leaf1", LEAF),
                    entryForTask(TaskEnded.class, "leaf1", LEAF)
                ),
                ordered(
                    entryForTask(SubtaskDefined.class, "node task", PAR_NODE, matchesSubtask("leaf2", LEAF)),
                    entryForTask(TaskStarted.class, "leaf2", LEAF),
                    entryForTask(InstructionsRan.class, "leaf2", LEAF),
                    entryForTask(TaskEnded.class, "leaf2", LEAF)
                ),
                ordered(
                    entryForTask(SubtaskDefined.class, "node task", PAR_NODE, matchesSubtask("leaf3", LEAF)),
                    entryForTask(TaskStarted.class, "leaf3", LEAF),
                    entryForTask(InstructionsRan.class, "leaf3", LEAF),
                    entryForTask(TaskEnded.class, "leaf3", LEAF)
                )
            ),
            entryForTask(BodyExecuted.class, "node task", PAR_NODE),
            entryForTask(SubtaskDefined.class, "node task", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            unordered(
                entryForTask(SubtaskIncorporated.class, "node task", PAR_NODE, matchesSubtask("leaf1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node task", PAR_NODE, matchesSubtask("leaf2", LEAF))
            ),
            entryForTask(SubtaskIncorporated.class, "node task", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskEnded.class, "node task", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root task", ROOT, matchesSubtask("node task", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root task", ROOT),
            entryForTask(TaskEnded.class, "root task", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", PAR_NODE,
                (nodeExec, nodeStorage) -> {
                    nodeExec.execute("leaf1", LEAF, (leafStorage) -> {

                    });
                    nodeExec.execute("leaf2", LEAF, (leafStorage) -> {

                    });
                    nodeExec.execute("leaf3", LEAF, (leafStorage) -> {

                    });
                },
                discard("leaf3")
            );
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
            entryForTask(InstructionsSkipped.class, "leaf task", LEAF),
            entryForTask(SubtaskIncorporated.class, "node task", SEQ_NODE, matchesSubtask("leaf task", LEAF)),
            entryForTask(BodyExecuted.class, "node task", SEQ_NODE),
            entryForTask(SubtaskIncorporated.class, "root task", ROOT, matchesSubtask("node task", SEQ_NODE)),
            entryForTask(BodyExecuted.class, "root task", ROOT)
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
            entryForTask(BodyChanged.class, "node task", SEQ_NODE),
            entryForTask(SubtaskDisowned.class, "node task", SEQ_NODE, matchesSubtask("leaf task", LEAF)),
            entryForTask(TaskOrphaned.class, "leaf task", LEAF),
            entryForTask(SubtaskDefined.class, "node task", SEQ_NODE, matchesSubtask("another leaf task", LEAF)),
            entryForTask(TaskStarted.class, "another leaf task", LEAF),
            entryForTask(InstructionsRan.class, "another leaf task", LEAF),
            entryForTask(TaskEnded.class, "another leaf task", LEAF),
            entryForTask(SubtaskIncorporated.class, "node task", SEQ_NODE, matchesSubtask("another leaf task", LEAF)),
            entryForTask(BodyExecuted.class, "node task", SEQ_NODE),
            entryForTask(TaskAmended.class, "node task", SEQ_NODE),
            entryForTask(TaskEnded.class, "node task", SEQ_NODE),
            entryForTask(SubtaskIncorporated.class, "root task", ROOT, matchesSubtask("node task", SEQ_NODE)),
            entryForTask(BodyExecuted.class, "root task", ROOT),
            entryForTask(TaskAmended.class, "root task", ROOT),
            entryForTask(TaskEnded.class, "root task", ROOT)
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
            entryForTask(InstructionsSkipped.class, "leaf task", LEAF),
            entryForTask(SubtaskIncorporated.class, "node task", SEQ_NODE, matchesSubtask("leaf task", LEAF)),
            entryForTask(BodyExtended.class, "node task", SEQ_NODE),
            entryForTask(SubtaskDefined.class, "node task", SEQ_NODE, matchesSubtask("another leaf task", LEAF)),
            entryForTask(TaskStarted.class, "another leaf task", LEAF),
            entryForTask(InstructionsRan.class, "another leaf task", LEAF),
            entryForTask(TaskEnded.class, "another leaf task", LEAF),
            entryForTask(SubtaskIncorporated.class, "node task", SEQ_NODE, matchesSubtask("another leaf task", LEAF)),
            entryForTask(BodyExecuted.class, "node task", SEQ_NODE),
            entryForTask(SubtaskIncorporated.class, "root task", ROOT, matchesSubtask("node task", SEQ_NODE)),
            entryForTask(BodyExecuted.class, "root task", ROOT),
            entryForTask(TaskAmended.class, "root task", ROOT),
            entryForTask(TaskEnded.class, "root task", ROOT)
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
            entryForTask(InstructionsSkipped.class, "leaf task", LEAF),
            entryForTask(SubtaskIncorporated.class, "node task", SEQ_NODE, matchesSubtask("leaf task", LEAF)),
            entryForTask(BodyNarrowed.class, "node task", SEQ_NODE),
            entryForTask(SubtaskDisowned.class, "node task", SEQ_NODE, matchesSubtask("another leaf task", LEAF)),
            entryForTask(TaskOrphaned.class, "another leaf task", LEAF),
            entryForTask(BodyExecuted.class, "node task", SEQ_NODE),
            entryForTask(TaskAmended.class, "node task", SEQ_NODE),
            entryForTask(TaskEnded.class, "node task", SEQ_NODE),
            entryForTask(SubtaskIncorporated.class, "root task", ROOT, matchesSubtask("node task", SEQ_NODE)),
            entryForTask(BodyExecuted.class, "root task", ROOT),
            entryForTask(TaskAmended.class, "root task", ROOT),
            entryForTask(TaskEnded.class, "root task", ROOT)
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
            entryForTask(TaskStarted.class, "root", ROOT),
            entryForTask(ExceptionCaught.class, "root", ROOT, p ->
                p.get1().getClassName().equals(AnException.class.getCanonicalName()) &&
                    p.get1().getMessage().equals("abc")
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
            entryForTask(TaskStarted.class, "root", ROOT),
            entryForTask(SubtaskDefined.class, "root", ROOT),
            entryForTask(TaskStarted.class, "node", SEQ_NODE),
            entryForTask(ExceptionCaught.class, "node", SEQ_NODE, p ->
                p.get1().getClassName().equals(AnException.class.getCanonicalName()) &&
                    p.get1().getMessage().equals("abc")
            ),
            entryForTask(TaskInterrupted.class, "root", ROOT)
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
            entryForTask(TaskStarted.class, "root", ROOT),
            entryForTask(SubtaskDefined.class, "root", ROOT),
            entryForTask(TaskStarted.class, "node", SEQ_NODE),
            entryForTask(SubtaskDefined.class, "node", SEQ_NODE),
            entryForTask(TaskStarted.class, "leaf", LEAF),
            entryForTask(ExceptionCaught.class, "leaf", LEAF, p ->
                p.get1().getClassName().equals(AnException.class.getCanonicalName()) &&
                    p.get1().getMessage().equals("abc")
            ),
            entryForTask(TaskInterrupted.class, "node", SEQ_NODE),
            entryForTask(TaskInterrupted.class, "root", ROOT)
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
            entryForTask(TaskStarted.class, "root", ROOT),
            entryForTask(SubtaskDefined.class, "root", ROOT, matchesSubtask("node", SEQ_NODE)),
            entryForTask(TaskStarted.class, "node", SEQ_NODE),
            entryForTask(SubtaskDefined.class, "node", SEQ_NODE, matchesSubtask("leaf1", LEAF)),
            entryForTask(TaskStarted.class, "leaf1", LEAF),
            entryForTask(InstructionsRan.class, "leaf1", LEAF),
            entryForTask(TaskEnded.class, "leaf1", LEAF),
            entryForTask(SubtaskIncorporated.class, "node", SEQ_NODE, matchesSubtask("leaf1", LEAF)),
            entryForTask(SubtaskDefined.class, "node", SEQ_NODE, matchesSubtask("leaf2", LEAF)),
            entryForTask(TaskStarted.class, "leaf2", LEAF),
            entryForTask(ExceptionCaught.class, "leaf2", LEAF, p ->
                p.get1().getClassName().equals(AnException.class.getCanonicalName()) &&
                    p.get1().getMessage().equals("abc")
            ),
            entryForTask(TaskInterrupted.class, "node", SEQ_NODE),
            entryForTask(TaskInterrupted.class, "root", ROOT)
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
                discard("l3")
            );
        });
        var listener = new AssertiveListener(
            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF)
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeInflated.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            //3 incorporations for leafs
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l2", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l3", LEAF))
            ),
            //1 merge leaf
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                acceptAll()
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, >l2, l3)) -> r(=n(>l1, l2, l3))")
    void deflateParallelNode(){
        var exec = FACTORY.create("deflateParallelNode");
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
                discard("l3")
            );
        });
        var listener = new AssertiveListener(
            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF)
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeDeflated.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                accept("l1")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, >l2, l3)) -> r(=n(l1, >l2, >l3))")
    void refilterParallelNode(){
        var exec = FACTORY.create("refilterParallelNode");
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
                discard("l3")
            );
        });
        var listener = new AssertiveListener(
            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF)
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeRefiltered.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            //2 incorporations for leafs
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l2", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l3", LEAF))
            ),
            //1 merge leaf
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                discard("l1")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, >l2, l3)) -> r(=n(>l1, >l2, l3, l4))")
    void extendParallelNode(){
        var exec = FACTORY.create("extendParallelNode");
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
                accept("l1", "l2")
            );
        });
        var listener = new AssertiveListener(

            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF),
                ordered(
                    entryForTask(BodyExtended.class, "node", PAR_NODE),
                    entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("l4", LEAF)),
                    entryForTask(TaskStarted.class, "l4", LEAF),
                    entryForTask(InstructionsRan.class, "l4", LEAF),
                    entryForTask(TaskEnded.class, "l4", LEAF)
                )
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            //2 incorporations for leafs
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l2", LEAF))
            ),
//todo I think this should be here, but it can wait to another version
//            entryForTask(InstructionsSkipped.class, "merge", LEAF),
            //1 merge leaf
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                    nt.execute("l4", LEAF, rw -> {
                    });
                },
                accept("l1", "l2")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, >l2, l3)) -> r(=n(>l1, >l2, l3, >l4))")
    void extendInflateParallelNode(){
        var exec = FACTORY.create("extendParallelNode");
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
                accept("l1", "l2")
            );
        });
        var listener = new AssertiveListener(

            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF),
                ordered(
                    entryForTask(BodyExtended.class, "node", PAR_NODE),
                    entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("l4", LEAF)),
                    entryForTask(TaskStarted.class, "l4", LEAF),
                    entryForTask(InstructionsRan.class, "l4", LEAF),
                    entryForTask(TaskEnded.class, "l4", LEAF)
                )
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeInflated.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            //3 incorporations for leafs
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l2", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l4", LEAF))
            ),
            //1 merge leaf
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                    nt.execute("l4", LEAF, rw -> {
                    });
                },
                accept("l1", "l2", "l4")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, >l2, l3)) -> r(=n(>l1, l2, l3, l4))")
    void extendDeflateParallelNode(){
        var exec = FACTORY.create("extendDeflateParallelNode");
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
                accept("l1", "l2")
            );
        });
        var listener = new AssertiveListener(

            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF),
                ordered(
                    entryForTask(BodyExtended.class, "node", PAR_NODE),
                    entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("l4", LEAF)),
                    entryForTask(TaskStarted.class, "l4", LEAF),
                    entryForTask(InstructionsRan.class, "l4", LEAF),
                    entryForTask(TaskEnded.class, "l4", LEAF)
                )
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeDeflated.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                    nt.execute("l4", LEAF, rw -> {
                    });
                },
                accept("l1")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, l2, l3)) -> r(=n(>l1, l2, >l3, l4))")
    void extendRefilterToOldParallelNode(){
        var exec = FACTORY.create("extendRefilterToOldParallelNode");
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
                accept("l1", "l2")
            );
        });
        var listener = new AssertiveListener(

            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF),
                ordered(
                    entryForTask(BodyExtended.class, "node", PAR_NODE),
                    entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("l4", LEAF)),
                    entryForTask(TaskStarted.class, "l4", LEAF),
                    entryForTask(InstructionsRan.class, "l4", LEAF),
                    entryForTask(TaskEnded.class, "l4", LEAF)
                )
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeRefiltered.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l3", LEAF))
            ),
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                    nt.execute("l4", LEAF, rw -> {
                    });
                },
                accept("l1", "l3")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, l2, l3)) -> r(=n(>l1, l2, l3, >l4))")
    void extendRefilterToNewParallelNode(){
        var exec = FACTORY.create("extendRefilterToNewParallelNode");
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
                accept("l1", "l2")
            );
        });
        var listener = new AssertiveListener(

            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF),
                entryForTask(InstructionsSkipped.class, "l3", LEAF),
                ordered(
                    entryForTask(BodyExtended.class, "node", PAR_NODE),
                    entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("l4", LEAF)),
                    entryForTask(TaskStarted.class, "l4", LEAF),
                    entryForTask(InstructionsRan.class, "l4", LEAF),
                    entryForTask(TaskEnded.class, "l4", LEAF)
                )
            ),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(ParallelNodeRefiltered.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l4", LEAF))
            ),
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
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
                    nt.execute("l4", LEAF, rw -> {
                    });
                },
                accept("l1", "l4")
            );
        });
        listener.end();
    }

    @Test
    @DisplayName("r(=n(>l1, l2, l3)) -> r(=n(>l1, >l2))")
    void narrowInflateParallelNode(){
        var exec = FACTORY.create("narrowInflateParallelNode");
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
                accept("l1")
            );
        });
        var listener = new AssertiveListener(

            unordered(
                entryForTask(InstructionsSkipped.class, "l1", LEAF),
                entryForTask(InstructionsSkipped.class, "l2", LEAF)
            ),
            entryForTask(BodyNarrowed.class, "node", PAR_NODE),
            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("l3", LEAF)), //merge leaf is disowned
            entryForTask(TaskOrphaned.class, "l3", LEAF),
            entryForTask(BodyExecuted.class, "node", PAR_NODE),
            entryForTask(TaskAmended.class, "node", PAR_NODE),
            //FIXME I believe that when narrowing, inflated may not be emitted
//            entryForTask(SubtaskDisowned.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)), //merge leaf is disowned
//            entryForTask(ParallelNodeInflated.class, "node", PAR_NODE),
//            entryForTask(TaskOrphaned.class, "merge", LEAF), //merge leaf is disowned
            entryForTask(SubtaskDefined.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
            entryForTask(TaskStarted.class, "merge", LEAF),
            entryForTask(InstructionsRan.class, "merge", LEAF),
            entryForTask(TaskEnded.class, "merge", LEAF),
            //3 incorporations for leafs
            unordered(
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l1", LEAF)),
                entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("l2", LEAF))
            ),
            //1 merge leaf
            entryForTask(SubtaskIncorporated.class, "node", PAR_NODE, matchesSubtask("merge", LEAF)),
//fixme yep, seems like it; should augmented be emitted, though? it should be emitted only if node is finished, and since it narrowed, then it isnt finished until there is ended after amended
//            entryForTask(ParallelNodeAugmented.class, "node", PAR_NODE),
            entryForTask(TaskEnded.class, "node", PAR_NODE),
            entryForTask(SubtaskIncorporated.class, "root", ROOT, matchesSubtask("node", PAR_NODE)),
            entryForTask(BodyExecuted.class, "root", ROOT),
            entryForTask(TaskAmended.class, "root", ROOT),
            entryForTask(TaskEnded.class, "root", ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root", ROOT, (rt, rs) -> {
            rt.execute("node", PAR_NODE,
                (nt, ns) -> {
                    nt.execute("l1", LEAF, rw -> {
                    });
                    nt.execute("l2", LEAF, rw -> {
                    });
                },
                accept("l1", "l2")
            );
        });
        listener.end();
    }
}
