package com.github.filipmalczak.storyteller.impl.tree.internal.expectations;

import java.util.List;

final class Any<Id extends Comparable<Id>> implements ExpectationsPolicy<Id> {
    @Override
    public List<Id> getCandidates(List<Id> expectations) {
        return expectations;
    }

    @Override
    public boolean noMatchingCandidatesTreatedAsConflict(boolean finished) {
        return false;
    }


}
