package com.github.filipmalczak.storyteller.utils.expectations;

import com.github.filipmalczak.storyteller.utils.expectations.Condition;
import lombok.Value;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Value
class AndCondition<E, T> implements Condition<E, T> {
    List<Condition<E, T>> parts;

    public AndCondition(Condition<E, T> left, Condition<E, T> right) {
        this(new ArrayList<>());
        parts.add(left);
        parts.add(right);
    }

    public AndCondition(List<Condition<E, T>> parts) {
        this.parts = parts;
    }

    @Override
    public boolean isSatisfied(E expected, T tracked) {
        return parts.stream().allMatch(x -> x.isSatisfied(expected, tracked));
    }

    @Override
    public String describe() {
        return "("+parts.stream().map(Condition::describe).collect(joining(" and "))+")";
    }

    @Override
    public Condition<E, T> and(Condition<E, T> another) {
        var newParts = new ArrayList<Condition<E, T>>();
        newParts.addAll(parts);
        if (another instanceof AndCondition and) {
            newParts.addAll(and.parts);
        } else {
            newParts.add(another);
        }
        return new AndCondition<>(newParts);
    }
}
