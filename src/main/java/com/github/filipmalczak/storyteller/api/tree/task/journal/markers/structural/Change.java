package com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.MetaDimension;

import java.util.stream.Stream;

@MetaDimension
public sealed interface Change permits Change.Decremental, Change.Incremental {
    /**
     * Entries that refer to an decremental change in some collection of IDs; "less things included than in previous run".
     */
    non-sealed interface Decremental<Id extends Comparable<Id>> extends Change {
        Stream<Id> getDecrement();
    }

    /**
     * Entries that refer to an incremental change in some collection of IDs; "more things included than in previous run".
     */
    non-sealed interface Incremental<Id extends Comparable<Id>> extends Change {
        Stream<Id> getIncrement();
    }
}
