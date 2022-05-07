package com.github.filipmalczak.storyteller.impl.tree.internal.expectations;

import java.util.List;

import static org.valid4j.Assertive.require;

final class Next<Id extends Comparable<Id>> implements ExpectationsPolicy<Id> {
    @Override
    public List<Id> getCandidates(List<Id> expectations) {
        return expectations.stream().limit(1).toList();
    }

    @Override
    public boolean noMatchingCandidatesTreatedAsConflict(boolean finished) {
        return finished;
    }
}
