package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import java.util.List;

public interface ReferencesSubtasks<Id extends Comparable<Id>> {
    List<Id> getReferences();
}
