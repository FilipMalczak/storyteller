package com.github.filipmalczak.storyteller.impl.stack;

import java.util.Collection;

public interface SubtaskOrderingStrategy<Id> {
    boolean hasExpectations();

    Collection<Id> getCandidatesForReusing();

    Id reuse(Id id);

    void onNoReusable(Collection<Id> candidates);
}
