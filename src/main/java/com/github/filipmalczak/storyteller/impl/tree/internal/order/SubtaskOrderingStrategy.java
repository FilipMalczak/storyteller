package com.github.filipmalczak.storyteller.impl.tree.internal.order;

import java.util.Collection;
import java.util.List;

public interface SubtaskOrderingStrategy<Id> {
    boolean hasExpectations();

    Collection<Id> getCandidatesForReusing();

    Id reuse(Id id);

    void onNoReusable(Collection<Id> candidates);

    //fixme assumes nullable and is ugly in many ways
    List<Id> getConflicts();
}
