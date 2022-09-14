package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.nextgen.TaskEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.EventsPersistence;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.List;
import java.util.Set;

import static com.github.filipmalczak.storyteller.impl.tree.internal.data.EventsPersistence.taskEntry;
import static org.valid4j.Assertive.require;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class EventsEmitter<Id extends Comparable<Id>> {
    @NonNull EventsPersistence<Id> manager;
    @NonNull JournalEntryFactory factory;

    public void defineSubtask(Task<Id, ?, ?> parent, Id subtask){
        manager.persist(parent, factory.subtaskDefined(subtask));
    }

    public void taskStarted(Task<Id, ?, ?> task){
        manager.persist(task, factory.taskStarted());
    }

    public void taskPerformed(Task<Id, ?, ?> task, boolean skip){
        if (task.getType().isLeaf()) {
            if (skip) {
                manager.persist(task, factory.instructionsSkipped());
            } else {
                manager.persist(task, factory.instructionsRan());
            }
        } else {
            require(!skip, "Only leaves can be skipped");
            manager.persist(task, factory.bodyExecuted());
        }
    }

    public void bodyChanged(Task<Id, ?, ?> task, Id pivot, List<Id> conflicting){
        //todo add pivot to the data
        manager.persist(task, factory.bodyChanged(pivot, conflicting));
    }

    public void bodyExtended(Task<Id, ?, ?> task, List<Id> added){
        manager.persist(task, factory.bodyExtended(added));
    }

    public void bodyNarrowed(Task<Id, ?, ?> task, List<Id> disappeared){
        manager.persist(task, factory.bodyNarrowed(disappeared));
    }

    public void taskAmended(Task<Id, ?, ?> task){
        manager.persist(task, factory.taskAmended());
    }

    public <D, T extends Enum<T> & TaskType> void subtasksDisowned(Task<Id, ?, ?> parent, List<Id> disowned){
        TaskEntry<Id>[] toRecord = new TaskEntry[disowned.size()*2];
        int[] i = { 0 };
        parent.getSubtasks(disowned.stream()).forEach(childTask -> {
            toRecord[i[0]++] = taskEntry(parent, factory.subtaskDisowned(childTask.getId()));
            toRecord[i[0]++] = taskEntry(childTask, factory.taskOrphaned());
        });
        manager.persist(toRecord);
    }

    public void exeptionCaught(Task<Id, ?, ?> culprit, Exception e){
        manager.persist(culprit, factory.exceptionCaught(e));
    }

    public void taskInterrupted(Task<Id, ?, ?> ancestor){
        manager.persist(ancestor, factory.taskInterrupted());
    }

    public void nodeInflated(Task<Id, ?, ?> node, Set<Id> disappeared){
        manager.persist(node, factory.nodeInflated(disappeared));
    }

    public void nodeDeflated(Task<Id, ?, ?> node, Set<Id> appeared){
        manager.persist(node, factory.nodeDeflated(appeared));
    }

    public void nodeRefiltered(Task<Id, ?, ?> node, Set<Id> appeared, Set<Id> disappeared){
        manager.persist(node, factory.nodeRefiltered(appeared, disappeared));
    }

    public void nodeAugmented(Task<Id, ?, ?> node){
        manager.persist(node, factory.nodeAugmented());
    }

    public void subtaskIncorporated(Task<Id, ?, ?> parent, Id child){
        manager.persist(parent, factory.subtaskIncorporated(child));
    }

    public void taskEnded(Task<Id, ?, ?> task){
        manager.persist(task, factory.taskEnded());
    }

}
