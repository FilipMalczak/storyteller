package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesCircumstances;

/**
 * Entries describing failure of a task execution.
 */
@DescribesCircumstances
public non-sealed interface FailureEvent extends TaskRuntimeEvent {
}
