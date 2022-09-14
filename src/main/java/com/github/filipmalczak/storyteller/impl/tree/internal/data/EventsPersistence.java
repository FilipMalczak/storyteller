package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.nextgen.TaskEntry;

import java.util.stream.Stream;

public interface EventsPersistence<Id extends Comparable<Id>> {


    void persist(TaskEntry<Id>... entries);

    public void persist(SessionEvent event);

    default void persist(Task<Id, ?, ?> task, JournalEntry entry){
        persist(taskEntry(task, entry));
    }

    Stream<JournalEntry> findEntriesByTaskId(Id taskId);
}
