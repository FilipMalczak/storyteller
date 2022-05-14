package com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural;

import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.DescribesData;

/**
 * Entries that refer to a single task.
 */
@DescribesData
public interface ReferencesSingleTask<Id extends Comparable<Id>> {
    Id getReference();
}
