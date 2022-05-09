package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import java.util.List;

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
