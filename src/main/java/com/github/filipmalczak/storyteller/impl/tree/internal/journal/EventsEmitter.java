package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.List;
import java.util.Set;

import static com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager.taskEntry;
import static org.valid4j.Assertive.require;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class EventsEmitter<Id extends Comparable<Id>> {
    @NonNull JournalEntryManager<Id> manager;
    @NonNull JournalEntryFactory factory;

    public void defineSubtask(Task<Id, ?, ?> parent, Id subtask){
        manager.record(parent, factory.subtaskDefined(subtask));
    }

    public void taskStarted(Task<Id, ?, ?> task){
        manager.record(task, factory.taskStarted());
    }

    public void taskPerformed(Task<Id, ?, ?> task, boolean skip){
        if (task.getType().isLeaf()) {
            if (skip) {
                manager.record(task, factory.instructionsSkipped());
            } else {
                manager.record(task, factory.instructionsRan());
            }
        } else {
            require(!skip, "Only leaves can be skipped");
            manager.record(task, factory.bodyExecuted());
        }
    }

    public void bodyChanged(Task<Id, ?, ?> task, Id pivot, List<Id> conflicting){
        //todo add pivot to the data
        manager.record(task, factory.bodyChanged(pivot, conflicting));
    }

    public void bodyExtended(Task<Id, ?, ?> task, List<Id> added){
        manager.record(task, factory.bodyExtended(added));
    }

    public void bodyNarrowed(Task<Id, ?, ?> task, List<Id> disappeared){
        manager.record(task, factory.bodyNarrowed(disappeared));
    }

    public void taskAmended(Task<Id, ?, ?> task){
        manager.record(task, factory.taskAmended());
    }

    public <D, T extends Enum<T> & TaskType> void subtasksDisowned(Task<Id, ?, ?> parent, List<Id> disowned){
        JournalEntryManager.TaskEntry<Id>[] toRecord = new JournalEntryManager.TaskEntry[disowned.size()*2];
        int[] i = { 0 };
        parent.getSubtasks(disowned.stream()).forEach(childTask -> {
            toRecord[i[0]++] = taskEntry(parent, factory.subtaskDisowned(childTask.getId()));
            toRecord[i[0]++] = taskEntry(childTask, factory.taskOrphaned());
        });
        manager.record(toRecord);
    }

    public void exeptionCaught(Task<Id, ?, ?> culprit, Exception e){
        manager.record(culprit, factory.exceptionCaught(e));
    }

    public void taskInterrupted(Task<Id, ?, ?> ancestor){
        manager.record(ancestor, factory.taskInterrupted());
    }

    public void nodeInflated(Task<Id, ?, ?> node, Set<Id> disappeared){
        manager.record(node, factory.nodeInflated(disappeared));
    }

    public void nodeDeflated(Task<Id, ?, ?> node, Set<Id> appeared){
        manager.record(node, factory.nodeDeflated(appeared));
    }

    public void nodeRefiltered(Task<Id, ?, ?> node, Set<Id> appeared, Set<Id> disappeared){
        manager.record(node, factory.nodeRefiltered(appeared, disappeared));
    }

    public void nodeAugmented(Task<Id, ?, ?> node){
        manager.record(node, factory.nodeAugmented());
    }

    public void subtaskIncorporated(Task<Id, ?, ?> parent, Id child){
        manager.record(parent, factory.subtaskIncorporated(child));
    }

    public void taskEnded(Task<Id, ?, ?> task){
        manager.record(task, factory.taskEnded());
    }

}
