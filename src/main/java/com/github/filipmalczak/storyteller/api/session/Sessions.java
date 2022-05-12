package com.github.filipmalczak.storyteller.api.session;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.session.listener.ListenerHandle;
import com.github.filipmalczak.storyteller.api.session.listener.SessionListener;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

public interface Sessions {
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Getter
    class AlreadyStartedException extends RuntimeException {
        @NonNull Session current;
        public AlreadyStartedException(Session current) {
            super("There already is a started session: "+current);
            this.current = current;
        }
    }

    /**
     * Start a sesssion. If there already is a started session, throw. Use {@link Sessions#end()} to avoid that.
     * @return non-null newly started session
     * @throws AlreadyStartedException if a session has already been started
     */
    Session start();

    /**
     * Start a session if there is no started one yet; if there is, return the already started one.
     * @return non-null session
     */
    Session getCurrent();

    /**
     * Discards current session. Next call to {@link Sessions#start()} (or {@link Sessions#getCurrent()}) will open one.
     */
    void end();

    <T extends SessionEvent> ListenerHandle addListener(Class<T> clazz, SessionListener<T> listener);
    <T extends JournalEntry> ListenerHandle addListener(Class<T> clazz, JournalListener<T> listener);

    //todo this can be tested by opening the session twice; since the mechanism is exactly the same, it should suffice to check validitiy
    default ListenerHandle addListener(SessionListener<SessionEvent> listener){
        return addListener(SessionEvent.class, listener);
    }

    default ListenerHandle addListener(JournalListener<JournalEntry> listener){
        return addListener(JournalEntry.class, listener);
    }
}
