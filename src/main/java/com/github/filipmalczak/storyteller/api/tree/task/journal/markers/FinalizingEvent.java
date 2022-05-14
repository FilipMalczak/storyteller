package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesCircumstances;

/**
 * These journal entries impact whether the task is considered finished or not.
 */
@DescribesCircumstances
public interface FinalizingEvent extends EntryPersistence.LifecycleEvent {
}
