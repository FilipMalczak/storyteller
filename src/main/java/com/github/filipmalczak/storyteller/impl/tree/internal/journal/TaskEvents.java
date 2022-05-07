package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager;

import java.util.List;

import static com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager.taskEntry;
import static org.valid4j.Assertive.require;

public interface TaskEvents<Id extends Comparable<Id>> {
    void defineSubtask(Id subtask);

    void taskStarted();

    void taskPerformed(boolean skip);

    void bodyChanged(List<Id> conflicting, Id pivot);

    void bodyExtended();

    void bodyNarrowed(List<Id> disappeared);

    void taskAmended();

    void subtasksDisowned(List<Id> disowned);

    void exeptionCaught(Exception e);

    void taskInterrupted();

    void nodeInflated();

    void nodeDeflated();

    void nodeRefiltered();

    void nodeAugmented();

    void subtaskIncorporated(Id child);

    void taskEnded();
}
