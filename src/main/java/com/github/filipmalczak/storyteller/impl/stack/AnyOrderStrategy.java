package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import lombok.Setter;

import java.util.*;
import java.util.stream.Stream;

public class AnyOrderStrategy<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements SubtaskOrderingStrategy<Id>{
    final List<Id> expectations;
    @Setter
    ExecutionFriend<Id, Definition, Type> friend;

    public AnyOrderStrategy(List<Id> expectations) {
        this.expectations = expectations;
    }

    @Override
    public boolean hasExpectations() {
        return !expectations.isEmpty();
    }

    @Override
    public Collection<Id> getCandidatesForReusing() {
        return expectations;
    }

    @Override
    public Id reuse(Id id) {
        expectations.remove(id);
        return id;
    }

    @Override
    public void onNoReusable(Collection<Id> candidates) {

    }
}