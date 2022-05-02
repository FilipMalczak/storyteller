package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.TaskManager;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.List;

import static com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager.taskEntry;
import static org.valid4j.Assertive.require;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class Events<Id extends Comparable<Id>> {
    @NonNull TaskManager<Id, ?, ?> taskManager; //todo resolver would suffice?
    @NonNull JournalEntryManager<Id> manager;
    @NonNull JournalEntryFactory factory;

    //fixme second param should be id
    public void defineSubtask(Task<Id, ?, ?> parent, Task<Id, ?, ?> subtask){
        manager.record(parent, factory.subtaskDefined(subtask.getId()));
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

    public <D, T extends Enum<T> & TaskType> void bodyChanged(Task<Id, ?, ?> task, List<Task<Id, D, T>> conflicting){
        manager.record(task, factory.bodyChanged(conflicting.stream().map(Task::getId).toList()));
    }

    public void bodyExtended(Task<Id, ?, ?> task){
        manager.record(task, factory.bodyExtended());
    }

    public void bodyShrunk(Task<Id, ?, ?> task, List<Id> disappeared){
        manager.record(task, factory.bodyShrunk(disappeared));
    }

    public void taskAmended(Task<Id, ?, ?> task){
        manager.record(task, factory.taskAmended());
    }

    public <D, T extends Enum<T> & TaskType> void subtasksDisowned(Task<Id, ?, ?> parent, List<Id> disowned){
        JournalEntryManager.TaskEntry<Id>[] toRecord = new JournalEntryManager.TaskEntry[disowned.size()*2];
        int i = 0;
        for (var child: disowned){
            var childTask = taskManager.getById(child);
            toRecord[i++] = taskEntry(parent, factory.subtaskDisowned(child));
            toRecord[i++] = taskEntry(childTask, factory.taskOrphaned());
        }
        manager.record(toRecord);
    }

    public void exeptionCaught(Task<Id, ?, ?> culprit, Exception e){
        manager.record(culprit, factory.exceptionCaught(e));
    }

    public void taskInterrupted(Task<Id, ?, ?> ancestor){
        manager.record(ancestor, factory.taskInterrupted());
    }

    public void nodeInflated(Task<Id, ?, ?> node){
        manager.record(node, factory.nodeInflated());
    }

    public void nodeDeflated(Task<Id, ?, ?> node){
        manager.record(node, factory.nodeDeflated());
    }

    public void nodeRefiltered(Task<Id, ?, ?> node){
        manager.record(node, factory.nodeRefiltered());
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
