package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import java.util.List;

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
    public void bodyExtended() {

    }

    @Override
    public void bodyNarrowed(List<Id> disappeared) {

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
    public void nodeInflated() {

    }

    @Override
    public void nodeDeflated() {

    }

    @Override
    public void nodeRefiltered() {

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
