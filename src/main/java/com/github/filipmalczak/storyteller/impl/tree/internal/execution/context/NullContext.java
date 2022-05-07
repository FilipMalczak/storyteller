package com.github.filipmalczak.storyteller.impl.tree.internal.execution.context;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.expectations.ExpectationsPolicy;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.NullEvents;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.TaskEvents;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class NullContext<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements ExecutionContext<Id, Definition, Type> {
    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void disown(List<Id> toDisown) {

    }

    @Override
    public void disownExpectations() {

    }

    @Override
    public boolean needsAmendment() {
        return false;
    }

    @Override
    public void requireAmendment() {

    }

    @Override
    public ExecutionContext parent() {
        return null;
    }

    @Override
    public Id id() {
        return null;
    }

    @Override
    public Task<Id, Definition, Type> task() {
        return null;
    }

    @Override
    public TaskEvents<Id> events() {
        return new NullEvents<>();
    }

    @Override
    public ExpectationsPolicy<Id> policy() {
        return ExpectationsPolicy.any();
    }

    @Override
    public List<Id> expectations() {
        return emptyList();
    }

    @Override
    public void reuseForSubtask(Id id) {
        //pass
    }

    @Override
    public IncrementalHistoryTracker<Id> history() {
        return null;
    }

    @Override
    public void incorporate(Id subtask, Map<Id, HistoryDiff<Id>> increment) {

    }

    @Override
    public Stream<Id> taskStack() {
        return Stream.empty();
    }
}
