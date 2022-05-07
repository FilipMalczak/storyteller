package com.github.filipmalczak.storyteller.impl.tree.internal.execution.context;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.expectations.ExpectationsPolicy;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.TaskEvents;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.valid4j.Assertive.require;

public interface ExecutionContext<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    boolean isStarted();
    boolean isFinished();
    default boolean isRoot(){
        return task().getType().isRoot();
    }
    void disown(List<Id> toDisown);
    void disownExpectations();
    boolean needsAmendment();
    void requireAmendment();
    ExecutionContext<Id, Definition, Type> parent();
    Id id();
    Task<Id, Definition, Type> task();
    TaskEvents<Id> events();
    ExpectationsPolicy<Id> policy();
    List<Id> expectations();
    void reuseForSubtask(Id id);
    IncrementalHistoryTracker<Id> history();
    void incorporate(Id subtask, Map<Id, HistoryDiff<Id>> increment);
    Stream<Id> taskStack(); //first - this is, last - root

    default void makeHistory(){
        var t = task();
        require(t != null, "Cannot make a null task part of history");
        var id = t.getId();
        var writing = t.getType().isWriting();
        taskStack().forEach(stackId -> history().add(stackId, id, writing));
    }

    //todo I think "incorporate"
}
