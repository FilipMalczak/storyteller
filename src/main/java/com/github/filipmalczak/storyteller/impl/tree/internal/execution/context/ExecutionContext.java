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

public interface ExecutionContext<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    boolean isStarted();
    boolean isFinished();
    void disown(List<Id> toDisown);
    void disownExpectations();
    boolean needsAmendment();
    void requireAmendment();
    ExecutionContext parent();
    Id id();
    Task<Id, Definition, Type> task();
    TaskEvents<Id> events();
    ExpectationsPolicy<Id> policy();
    List<Id> expectations();
    void reuseForSubtask(Id id);
    IncrementalHistoryTracker<Id> history();
    void incorporate(Id subtask, Map<Id, HistoryDiff<Id>> increment);

    //todo I think "incorporate"
}
