package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.session.Sessions;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.util.Optional;

public interface SessionManager extends Sessions {
    Optional<Session> findById(String id);

    default Session getById(String id){
        return findById(id).get(); //todo exception
    }

    Session getCurrent();

    <T extends JournalEntry> void emit(Task owner, T entry);
}
