package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
public class BoundEventsEmitter<Id extends Comparable<Id>> implements TaskEvents<Id> {
    Task<Id, ?, ?> task;
    EventsEmitter<Id> emitter;

    @Override
    public void defineSubtask(Id subtask) {
        emitter.defineSubtask(task, subtask);
    }

    @Override
    public void taskStarted() {
        emitter.taskStarted(task);
    }

    @Override
    public void taskPerformed(boolean skip) {
        emitter.taskPerformed(task, skip);
    }

    @Override
    public void bodyChanged(List<Id> conflicting, Id pivot) {
        emitter.bodyChanged(task, pivot, conflicting);
    }

    @Override
    public void bodyExtended(List<Id> added) {
        emitter.bodyExtended(task, added);
    }

    @Override
    public void bodyNarrowed(List<Id> removed) {
        emitter.bodyNarrowed(task, removed);
    }

    @Override
    public void taskAmended() {
        emitter.taskAmended(task);
    }

    @Override
    public void subtasksDisowned(List<Id> disowned) {
        emitter.subtasksDisowned(task, disowned);
    }

    @Override
    public void exeptionCaught(Exception e) {
        emitter.exeptionCaught(task, e);
    }

    @Override
    public void taskInterrupted() {
        emitter.taskInterrupted(task);
    }

    //todo should inflated, deflated and refiltered follow the example of extended, narrowed and changed and include reasons?
    @Override
    public void nodeInflated(Set<Id> appeared) {
        emitter.nodeInflated(task, appeared);
    }

    @Override
    public void nodeDeflated(Set<Id> disappeared) {
        emitter.nodeDeflated(task, disappeared);
    }

    @Override
    public void nodeRefiltered(Set<Id> appeared, Set<Id> disappeared) {
        emitter.nodeRefiltered(task, appeared, disappeared);
    }

    @Override
    public void nodeAugmented() {
        emitter.nodeAugmented(task);
    }

    @Override
    public void subtaskIncorporated(Id child) {
        emitter.subtaskIncorporated(task, child);
    }

    @Override
    public void taskEnded() {
        emitter.taskEnded(task);
    }
}
