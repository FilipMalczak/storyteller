package com.github.filipmalczak.storyteller.api.session;

import com.github.filipmalczak.storyteller.api.common.Maker;
import com.github.filipmalczak.storyteller.api.session.LiveSession;
import com.github.filipmalczak.storyteller.api.session.PastSession;
import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.session.listener.ListenerHandle;
import com.github.filipmalczak.storyteller.api.session.listener.SessionListener;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.util.stream.Stream;

public interface SessionManager<TaskId extends Comparable<TaskId>, NoSql> extends AutoCloseable, Maker<LiveSession<TaskId, NoSql>> {
    Stream<PastSession<TaskId, NoSql>> pastSessions();

    boolean isClosed();

    <T extends SessionEvent> ListenerHandle addListener(Class<T> clazz, SessionListener<T> listener);
    <T extends JournalEntry> ListenerHandle addListener(Class<T> clazz, JournalListener<T> listener);

    default ListenerHandle addListener(SessionListener<SessionEvent> listener){
        return addListener(SessionEvent.class, listener);
    }

    default ListenerHandle addListener(JournalListener<JournalEntry> listener){
        return addListener(JournalEntry.class, listener);
    }


}
