package com.github.filipmalczak.storyteller.impl.tree.internal.expectations;

import java.util.List;

public sealed interface ExpectationsPolicy<Id extends Comparable<Id>> permits Any, Next {
    /**
     * Take all the already defined subtask IDs, return IDs that can be possibly used as ID of next subtask in runtime.
     */
    List<Id> getCandidates(List<Id> expectations);

    boolean noMatchingCandidatesTreatedAsConflict(boolean finished);

    static <Id extends Comparable<Id>> ExpectationsPolicy<Id> any(){
        return new Any<>();
    }

    static <Id extends Comparable<Id>> ExpectationsPolicy<Id> next(){
        return new Next<>();
    }
}
