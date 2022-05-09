package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.session.listener.LoggingJournalListener;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.AssertiveListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.filipmalczak.storyteller.utils.AssertiveListener.expect;

public class JournalViaListenerTests {
    private static final TestTreeFactory FACTORY = new TestTreeFactory("DataConsistencyTests");



    @Test
    @DisplayName("r(n(l))")
    void singleRun(){
        var exec = FACTORY.create("singleRun");
        var listener = new AssertiveListener(
            expect(TaskStarted.class, "root task", TrivialTaskType.ROOT),
            expect(SubtaskDefined.class, "root task", TrivialTaskType.ROOT),
            expect(TaskStarted.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskDefined.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskStarted.class, "leaf task", TrivialTaskType.LEAF),
            expect(InstructionsRan.class, "leaf task", TrivialTaskType.LEAF),
            expect(TaskEnded.class, "leaf task", TrivialTaskType.LEAF),
            expect(SubtaskIncorporated.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(BodyExecuted.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskEnded.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", TrivialTaskType.ROOT),
            expect(BodyExecuted.class, "root task", TrivialTaskType.ROOT),
            expect(TaskEnded.class, "root task", TrivialTaskType.ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    @Test
    @DisplayName("r(n(l)) x2")
    void reRunWithoutChanges(){
        var exec = FACTORY.create("reRunWithoutChanges");
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(InstructionsSkipped.class, "leaf task", TrivialTaskType.LEAF),
            expect(SubtaskIncorporated.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(BodyExecuted.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", TrivialTaskType.ROOT),
            expect(BodyExecuted.class, "root task", TrivialTaskType.ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    @Test
    @DisplayName("r(n(l1)) -> r(n(l2))")
    void reRunWithChangedLeaf(){
        var exec = FACTORY.create("reRunWithChangedLeaf");
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(BodyChanged.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskDisowned.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskOrphaned.class, "leaf task", TrivialTaskType.LEAF),
            expect(SubtaskDefined.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskStarted.class, "another leaf task", TrivialTaskType.LEAF),
            expect(InstructionsRan.class, "another leaf task", TrivialTaskType.LEAF),
            expect(TaskEnded.class, "another leaf task", TrivialTaskType.LEAF),
            expect(SubtaskIncorporated.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(BodyExecuted.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskAmended.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskEnded.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", TrivialTaskType.ROOT),
            expect(BodyExecuted.class, "root task", TrivialTaskType.ROOT),
            expect(TaskAmended.class, "root task", TrivialTaskType.ROOT),
            expect(TaskEnded.class, "root task", TrivialTaskType.ROOT)
        );
        exec.getSessions().addListener(listener);
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("another leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }

    //todo r(n1(l)) -> r(n2(l))

    @Test
    @DisplayName("r(n(l1)) -> r(n(l1, l2))")
    @Disabled("This doesn't work yet, but its time to go to sleep")
    void reRunWithAddedLeaf(){
        var exec = FACTORY.create("reRunWithAddedLeaf");
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        var listener = new AssertiveListener(
            expect(BodyExecuted.class, "root task", TrivialTaskType.ROOT),
            expect(BodyExecuted.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(InstructionsSkipped.class, "leaf task", TrivialTaskType.LEAF),
            expect(SubtaskIncorporated.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(BodyExtended.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskDefined.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskStarted.class, "another leaf task", TrivialTaskType.LEAF),
            expect(InstructionsRan.class, "another leaf task", TrivialTaskType.LEAF),
            expect(TaskEnded.class, "another leaf task", TrivialTaskType.LEAF),
            expect(SubtaskIncorporated.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskAmended.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(TaskEnded.class, "node task", TrivialTaskType.SEQ_NODE),
            expect(SubtaskIncorporated.class, "root task", TrivialTaskType.ROOT),
            expect(TaskAmended.class, "root task", TrivialTaskType.ROOT),
            expect(TaskEnded.class, "root task", TrivialTaskType.ROOT)
        );
        exec.getSessions().addListener(listener);
//        exec.getSessions().addListener(new LoggingJournalListener<>());
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.execute("node task", TrivialTaskType.SEQ_NODE, (nodeExec, nodeStorage) -> {
                nodeExec.execute("leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
                nodeExec.execute("another leaf task", TrivialTaskType.LEAF, (leafStorage) -> {

                });
            });
        });
        listener.end();
    }
}
