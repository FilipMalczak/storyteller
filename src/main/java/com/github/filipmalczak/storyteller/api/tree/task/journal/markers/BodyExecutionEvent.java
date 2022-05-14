package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesCircumstances;

/**
 * Entries that describe performing the body of the task - be it by running or skipping.
 */
@DescribesCircumstances
public non-sealed interface BodyExecutionEvent extends TaskRuntimeEvent {
}
