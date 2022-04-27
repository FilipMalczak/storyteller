package com.github.filipmalczak.storyteller.impl.tree;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.impl.testimpl.TestTreeFactory;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.testimpl.StringStringDoc.getOr;
import static com.github.filipmalczak.storyteller.impl.testimpl.StringStringDoc.put;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChoiceTests {
    private static final TestTreeFactory FACTORY = new TestTreeFactory("TaskSkippingTests");

    //todo tests for non-leaf choice options

    @Test
    void simplestChoiceWithFiles(){
        var exec = FACTORY.create("runThreeTasks");
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.chooseBranchToProceed("choice task", TrivialTaskType.NODE, (nodeExec, nodeStorage, insight) -> {
                var t1 = nodeExec.execute("a", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("a"));
                });
                var t2 = nodeExec.execute("aa", TrivialTaskType.LEAF, (leafStorage) -> {
                    leafStorage.files().writer(Path.of("foo.txt"), w -> w.println("aa"));
                });
                return Stream.of(t1, t2).sorted(Comparator.comparing((Task t) -> insight.into(t).files().readAll(Path.of("foo.txt")).length()).reversed()).findFirst().get();
            });
            assertThat(rootStorage.files().readAll(Path.of("foo.txt")), equalTo("aa"));
        });
    }


    @Test
    void simplestChoiceWithDocs(){
        var exec = FACTORY.create("runThreeTasks");
        //the prefixes are there, because if insights fail, then it will fail over to definition;
        // by that account the "aa" will be chosen, while we intentionally put longer value for "a"
        var longer = "a______";
        var shorter = "aa__";
        exec.execute("root task", TrivialTaskType.ROOT, (rootExec, rootStorage) -> {
            rootExec.chooseBranchToProceed("choice task", TrivialTaskType.NODE, (nodeExec, nodeStorage, insight) -> {
                var t1 = nodeExec.execute("a", TrivialTaskType.LEAF, (leafStorage) -> {
                    //
                    put(leafStorage, "foo", longer);
                });
                var t2 = nodeExec.execute("aa", TrivialTaskType.LEAF, (leafStorage) -> {
                    put(leafStorage, "foo", shorter);
                });
                return Stream.of(t1, t2)
                    .sorted(
                        Comparator.<Task<String, String, TrivialTaskType>, Integer>comparing(
                            t ->
                                getOr(insight.into(t), "foo", t.getDefinition()).length()
                        )
                            .reversed())
                    .findFirst()
                    .get();
            });
            assertThat(getOr(rootStorage, "foo", "N/A"), equalTo(longer));
        });
    }
}
