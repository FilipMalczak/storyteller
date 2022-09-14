package com.github.filipmalczak.storyteller.impl.nextgen;

import com.github.filipmalczak.storyteller.api.session.LiveSession;
import com.github.filipmalczak.storyteller.api.session.PastSession;
import com.github.filipmalczak.storyteller.api.session.SessionManager;
import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.session.listener.ListenerHandle;
import com.github.filipmalczak.storyteller.api.session.listener.SessionListener;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.dizitart.no2.Nitrite;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NitriteTreeSessionManager<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements SessionManager<Id, Nitrite> {
    @NonNull NitriteTreeConfig<Id, Definition, Type> config;

    //todo exclude from builder
    @Getter @NonFinal boolean closed = false;
    List<LiveSession<Id, Nitrite>> madeSessions = new ArrayList<>();
    ManagedListeners listeners = new ManagedListeners(new ArrayList<>(), new ArrayList<>());

    @Override
    public LiveSession<Id, Nitrite> make() {
        require(!closed, "Session manager cannot be closed");
        return null;
    }

    @Override
    public Stream<PastSession<Id, Nitrite>> pastSessions() {
        require(!closed, "Session manager cannot be closed");
        return null;
    }

    @Override
    public <T extends SessionEvent> ListenerHandle addListener(Class<T> clazz, SessionListener<T> listener) {
        require(!closed, "Session manager cannot be closed");
        var definition = new ManagedListeners.SessionListenerDefinition(clazz, listener);
        listeners.sessionListeners().add(definition);
        return () -> listeners.sessionListeners().remove(definition);
    }

    @Override
    public <T extends JournalEntry> ListenerHandle addListener(Class<T> clazz, JournalListener<T> listener) {
        require(!closed, "Session manager cannot be closed");
        var definition = new ManagedListeners.JournalListenerDefinition(clazz, listener);
        listeners.journalListeners().add(definition);
        return () -> listeners.journalListeners().remove(definition);
    }

    @Override
    public void close() {
        if (!closed) {
            madeSessions.stream().forEach(LiveSession::close);
            closed = true;
        }
    }
}
