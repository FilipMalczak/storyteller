package com.github.filipmalczak.storyteller.api.tree.task.journal.markers;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesCircumstances;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.MetaDimension;

@MetaDimension
public sealed interface EntryPersistence permits EntryPersistence.LifecycleEvent, EntryPersistence.SessionEvent {
    /**
     * These journal entries describe events that impact session-agnostic task lifecycle.
     */
    @DescribesCircumstances
    non-sealed interface LifecycleEvent extends EntryPersistence {
    }

    /**
     * Entries emitted per-session.
     */
    @DescribesCircumstances
    sealed interface SessionEvent extends EntryPersistence permits TaskRuntimeEvent, TreeStructureEvent {
    }
}
