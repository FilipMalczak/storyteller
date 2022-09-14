package com.github.filipmalczak.storyteller.impl.nextgen;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.session.listener.SessionListener;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.util.List;

public record ManagedListeners(List<SessionListenerDefinition> sessionListeners, List<JournalListenerDefinition> journalListeners) {
    record SessionListenerDefinition(
        Class<? extends SessionEvent> qualifier,
        SessionListener listener
    ) {
        public void consume(SessionEvent event){
            if (qualifier.isInstance(event)){
                listener.on(event);
            }
        }
    }

    record JournalListenerDefinition(
        Class<? extends JournalEntry> qualifier,
        JournalListener listener
    ) {
        public void consume(Task owner, JournalEntry entry){
            if (qualifier.isInstance(null)){
                listener.on(owner, entry);
            }
        }
    }

    public void consume(SessionEvent event){
        for (var l: sessionListeners)
            l.consume(event);
    }

    public void consume(Task owner, JournalEntry entry){
        for (var l: journalListeners)
            l.consume(owner, entry);
    }

}
