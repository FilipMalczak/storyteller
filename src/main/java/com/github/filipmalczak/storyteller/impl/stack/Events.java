package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.impl.stack.data.JournalEntryManager;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.List;

import static com.github.filipmalczak.storyteller.impl.stack.data.JournalEntryManager.taskEntry;
import static org.valid4j.Assertive.require;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Flogger
public class Events<TaskId extends Comparable<TaskId>> {
    @NonNull JournalEntryManager<TaskId> manager;
    @NonNull JournalEntryFactory factory;

    public void defineSubtask(Task<TaskId, ?, ?> parent, Task<TaskId, ?, ?> subtask){
        manager.record(parent, factory.subtaskDefined(subtask));
    }

    public void taskStarted(Task<TaskId, ?, ?> task){
        manager.record(task, factory.taskStarted());
    }

    public void taskPerformed(Task<TaskId, ?, ?> task, boolean skip){
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

    public <D, T extends Enum<T> & TaskType> void bodyChanged(Task<TaskId, ?, ?> task, List<Task<TaskId, D, T>> conflicting){
        manager.record(task, factory.bodyChanged(conflicting.stream().map(t -> (Task) t).toList()));
    }

    public void bodyExtended(Task<TaskId, ?, ?> task){
        manager.record(task, factory.bodyExtended());
    }

    public void bodyShrunk(Task<TaskId, ?, ?> task, List<Task> disappeared){
        manager.record(task, factory.bodyShrunk(disappeared));
    }

    public void taskAmended(Task<TaskId, ?, ?> task){
        manager.record(task, factory.taskAmended());
    }

    public <D, T extends Enum<T> & TaskType> void subtasksDisowned(Task<TaskId, ?, ?> parent, List<Task<TaskId, D, T>> disowned){
        JournalEntryManager.TaskEntry<TaskId>[] toRecord = new JournalEntryManager.TaskEntry[disowned.size()*2];
        int i = 0;
        for (var child: disowned){
            toRecord[i++] = taskEntry(parent, factory.subtaskDisowned(child));
            toRecord[i++] = taskEntry(child, factory.taskOrphaned());
        }
        manager.record(toRecord);
    }

    public void exeptionCaught(Task<TaskId, ?, ?> culprit, Exception e){
        manager.record(culprit, factory.exceptionCaught(e));
    }

    public void taskInterrupted(Task<TaskId, ?, ?> ancestor){
        manager.record(ancestor, factory.taskInterrupted());
    }

    public void decided(Task<TaskId, ?, ?> choice, Task decision){
        manager.record(choice, factory.choiceWasMade(decision));
    }

    public void subtaskIncorporated(Task<TaskId, ?, ?> parent, Task child){
        manager.record(parent, factory.subtaskIncorporated(child));
    }

    public void taskEnded(Task<TaskId, ?, ?> task){
        manager.record(task, factory.taskEnded());
    }

}
