package com.github.filipmalczak.storyteller.impl.tree.internal.history;

import lombok.Value;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@Value
public class HistoryDiff<Id> {
    List<Id> addedAncestors;
    List<Id> addedWritingAncestors;

    public HistoryDiff<Id> since(Id snapshotPoint) {
        List<Id> newAll = addedAncestors.stream().takeWhile(x -> !x.equals(snapshotPoint)).toList();
        return new HistoryDiff<>(newAll, addedWritingAncestors.stream().takeWhile(x -> newAll.contains(x)).toList());
    }

    public HistoryDiff<Id> and(Id ancestor, boolean isLeaf){
        return new HistoryDiff<>(
            Stream.concat(Stream.of(ancestor), addedAncestors.stream()).toList(),
            Stream.concat(
                isLeaf ? Stream.of(ancestor) : Stream.empty(),
                addedWritingAncestors.stream()
            ).toList()
        );
    }

    public HistoryDiff<Id> and(HistoryDiff<Id> another){
        return new HistoryDiff<>(
            Stream.concat(another.addedAncestors.stream(), addedAncestors.stream()).toList(),
            Stream.concat(another.addedWritingAncestors.stream(), addedWritingAncestors.stream()).toList()
        );
    }

    public static <Id> HistoryDiff<Id> empty(){
        return new HistoryDiff<>(emptyList(), emptyList());
    }
}
