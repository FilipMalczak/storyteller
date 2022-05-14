package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import java.util.List;
import java.util.Set;

public interface TaskEvents<Id extends Comparable<Id>> {
    void defineSubtask(Id subtask);

    void taskStarted();

    void taskPerformed(boolean skip);

    void bodyChanged(List<Id> conflicting, Id pivot);

    void bodyExtended(List<Id> added);

    void bodyNarrowed(List<Id> removed);

    void taskAmended();

    void subtasksDisowned(List<Id> disowned);

    void exeptionCaught(Exception e);

    void taskInterrupted();

    void nodeInflated(Set<Id> disappeared);

    void nodeDeflated(Set<Id> appeared);

    void nodeRefiltered(Set<Id> appeared, Set<Id> disappeared);

    void nodeAugmented();

    void subtaskIncorporated(Id child);

    void taskEnded();
}
