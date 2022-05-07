package com.github.filipmalczak.storyteller.impl.tree.internal.execution.context;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.expectations.ExpectationsPolicy;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.BoundEventsEmitter;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.EventsEmitter;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.TaskEvents;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder
public class ContextImpl<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements ExecutionContext<Id, Definition, Type> {
    ExecutionContext<Id, Definition, Type> parent;
    Task<Id, Definition, Type> task;
    IncrementalHistoryTracker<Id> history;
    EventsEmitter<Id> emitter;
    ExpectationsPolicy<Id> policy;
    List<Task<Id, Definition, Type>> expectations;
    @Builder.Default
    @NonFinal boolean needsAmendment = false;

    @Getter(lazy = true)
    TaskEvents<Id> events = new BoundEventsEmitter<Id>(task, emitter);

//    public ContextImpl(ExecutionContext<Id, Definition, Type> parent, Task<Id, Definition, Type> task, ExpectationsPolicy<Id> policy, EventsEmitter<Id> eventsEmitter) {
//        this.parent = parent;
//        this.task = task;
//        this.emitter = eventsEmitter;
//        this.events = new BoundEventsEmitter<>(task, eventsEmitter);
//        this.policy = policy;
//        this.expectations = new LinkedList<>(task.getSubtasks().toList());
//        this.needsAmendment = false;
//    }

    @Override
    public boolean isStarted() {
        return task.isStarted();
    }

    @Override
    public boolean isFinished() {
        return task.isFinished();
    }

    @Override
    public void disown(List<Id> subtasks) {
        events().subtasksDisowned(subtasks);
        //slightly leaking abstraction - if we disown merge leaf, we're soon stuck with a finished execution that needs amending
        requireAmendment();
        parent.disownExpectations();
    }

    @Override
    public void disownExpectations() {
        disown(expectations.stream().map(Task::getId).toList());
        expectations.clear();
    }

    @Override
    public boolean needsAmendment() {
        return needsAmendment;
    }

    @Override
    public void requireAmendment() {
        needsAmendment = true;
    }

    @Override
    public ExecutionContext parent() {
        return parent;
    }

    @Override
    public Id id() {
        return task.getId();
    }

    @Override
    public Task<Id, Definition, Type> task() {
        return task;
    }

    @Override
    public TaskEvents<Id> events() {
        return getEvents();
    }

    @Override
    public ExpectationsPolicy<Id> policy() {
        return policy;
    }

    @Override
    public List<Id> expectations() {
        return expectations.stream().map(Task::getId).toList();
    }

    @Override
    public void reuseForSubtask(Id id) {
        expectations.remove(id);
    }

    @Override
    public IncrementalHistoryTracker<Id> history() {
        return history;
    }

    @Override
    public void incorporate(Id subtask, Map<Id, HistoryDiff<Id>> increment) {
        history.apply(increment);
        events().subtaskIncorporated(subtask);
    }
}
