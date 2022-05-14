package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesCircumstances;

/**
 * Entries that impact task tree structure.
 */
@DescribesCircumstances
public non-sealed interface TreeStructureEvent extends EntryPersistence.SessionEvent {
}
