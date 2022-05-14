package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import java.util.List;
import java.util.Set;

public class NullEvents<Id extends Comparable<Id>> implements TaskEvents<Id> {

    @Override
    public void defineSubtask(Id subtask) {

    }

    @Override
    public void taskStarted() {

    }

    @Override
    public void taskPerformed(boolean skip) {

    }

    @Override
    public void bodyChanged(List<Id> conflicting, Id pivot) {

    }

    @Override
    public void bodyExtended(List<Id> added) {

    }

    @Override
    public void bodyNarrowed(List<Id> removed) {

    }

    @Override
    public void taskAmended() {

    }

    @Override
    public void subtasksDisowned(List<Id> disowned) {

    }

    @Override
    public void exeptionCaught(Exception e) {

    }

    @Override
    public void taskInterrupted() {

    }

    @Override
    public void nodeInflated(Set<Id> disappeared) {

    }

    @Override
    public void nodeDeflated(Set<Id> appeared) {

    }

    @Override
    public void nodeRefiltered(Set<Id> appeared, Set<Id> disappeared) {

    }

    @Override
    public void nodeAugmented() {

    }

    @Override
    public void subtaskIncorporated(Id child) {

    }

    @Override
    public void taskEnded() {

    }
}
