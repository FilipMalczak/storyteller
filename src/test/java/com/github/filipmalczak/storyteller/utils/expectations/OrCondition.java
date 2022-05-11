package com.github.filipmalczak.storyteller.utils.expectations;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Value
public class OrCondition<E, T> implements Condition<E, T> {
    List<Condition<E, T>> parts;

    public OrCondition(Condition<E, T> left, Condition<E, T> right) {
        this(new ArrayList<>());
        parts.add(left);
        parts.add(right);
    }

    public OrCondition(List<Condition<E, T>> parts) {
        this.parts = parts;
    }

    @Override
    public boolean isSatisfied(E expected, T tracked) {
        return parts.stream().anyMatch(x -> x.isSatisfied(expected, tracked));
    }

    @Override
    public String describe() {
        return "("+parts.stream().map(Condition::describe).collect(joining(" or "))+")";
    }

    @Override
    public Condition<E, T> or(Condition<E, T> another) {
        var newParts = new ArrayList<Condition<E, T>>();
        newParts.addAll(parts);
        if (another instanceof OrCondition or) {
            newParts.addAll(or.parts);
        } else {
            newParts.add(another);
        }
        return new OrCondition<>(newParts);
    }
}
