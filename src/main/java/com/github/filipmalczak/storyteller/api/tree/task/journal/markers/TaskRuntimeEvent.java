package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesCircumstances;

/**
 * Entries concerning execution of the task.
 */
@DescribesCircumstances
public sealed interface TaskRuntimeEvent extends EntryPersistence.SessionEvent permits BodyExecutionEvent, FailureEvent {
}
