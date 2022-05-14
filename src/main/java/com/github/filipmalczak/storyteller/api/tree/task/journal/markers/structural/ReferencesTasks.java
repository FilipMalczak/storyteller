package com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesData;

import java.util.stream.Stream;

/**
 * Entries that refer to a collection of tasks.
 */
@DescribesData
public interface ReferencesTasks<Id extends Comparable<Id>> {
    Stream<Id> getReferences();
}
