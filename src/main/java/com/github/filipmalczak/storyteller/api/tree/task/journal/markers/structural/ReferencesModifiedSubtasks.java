package com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesData;

import java.util.stream.Stream;

public sealed interface ReferencesModifiedSubtasks<Id extends Comparable<Id>> extends ReferencesTasks<Id>
    permits ReferencesModifiedSubtasks.Added, ReferencesModifiedSubtasks.Removed, ReferencesModifiedSubtasks.AddedAndRemoved {
    /**
     * Entries that describe new subtasks being added to the parent task definition.
     */
    @DescribesData
    non-sealed interface Added<Id extends Comparable<Id>> extends ReferencesModifiedSubtasks<Id>, Change.Incremental<Id> {
        default Stream<Id> getReferences(){
            return getIncrement();
        }
    }

    /**
     * Entries that describe subtasks being removed from the parent task definition.
     */
    @DescribesData
    non-sealed interface Removed<Id extends Comparable<Id>> extends ReferencesModifiedSubtasks<Id>, Change.Decremental<Id> {
        default Stream<Id> getReferences(){
            return getDecrement();
        }
    }

    /**
     * Entries that describe subtasks being both added to and removed from the parent task definition.
     */
    @DescribesData
    non-sealed interface AddedAndRemoved<Id extends Comparable<Id>> extends ReferencesModifiedSubtasks<Id>, Added<Id>, Removed<Id> {

        default Stream<Id> getReferences(){
            return Stream.concat(getIncrement(), getDecrement());
        }
    }
}
