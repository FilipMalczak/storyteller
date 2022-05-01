package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.session.events.SessionAlreadyStarted;
import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.session.events.SessionStarted;
import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.session.listener.ListenerHandle;
import com.github.filipmalczak.storyteller.api.session.listener.SessionListener;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.SessionData;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.objects.ObjectRepository;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.valid4j.Assertive.require;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NitriteSessionManager implements SessionManager {
    @Setter(AccessLevel.PACKAGE) @NonNull ObjectRepository<SessionData> repository;
    Session current;

    @Value
    private static class JournalListenerDefinition<T extends JournalEntry>{
        @NonNull Class<T> entryType;
        @NonNull JournalListener<T> listener;
    }
    @Value
    private static class SessionListenerDefinition<T extends SessionEvent>{
        @NonNull Class<T> eventType;
        @NonNull SessionListener<T> listener;
    }

    final Set<JournalListenerDefinition<?>> journalListeners = new HashSet<>();
    final Set<SessionListenerDefinition<?>> sessionListeners = new HashSet<>();

    @Override
    public Optional<Session> findById(String id) {
        var found = repository.find(eq("id", id)).toList();
        require(found.size() < 2, "Session ID needs to be unique");
        if (found.isEmpty())
            return Optional.empty();
        return Optional.of(found.get(0).toSession());
    }

    @Override
    public Session getCurrent() {
        if (current == null){
            return start();
        }
        return current;
    }

    @Override
    public <T extends JournalEntry> void emit(Task owner, T entry) {
        for (var definition: journalListeners) {
            if (definition.entryType.isInstance(entry)) {
                ((JournalListener<T>)definition.listener).on(owner, (T) definition.entryType.cast(entry));
            }
        }
    }

    private <T extends SessionEvent> void emit(T event) {
        for (var definition: sessionListeners) {
            if (definition.eventType.isInstance(event)) {
                ((SessionListener<T>)definition.listener).on((T) definition.eventType.cast(event));
            }
        }
    }

    @Override
    public void end() {
        var prev = current;
        current = null;
        emit(new SessionAlreadyStarted(prev));
    }

    @Override
    public <T extends SessionEvent> ListenerHandle addListener(Class<T> clazz, SessionListener<T> listener) {
        var definition = new SessionListenerDefinition<T>(clazz, listener);
        sessionListeners.add(definition);
        return () -> sessionListeners.remove(definition);
    }

    @Override
    public <T extends JournalEntry> ListenerHandle addListener(Class<T> clazz, JournalListener<T> listener) {
        var definition = new JournalListenerDefinition<T>(clazz, listener);
        journalListeners.add(definition);
        return () -> journalListeners.remove(definition);
    }

    @SneakyThrows
    public Session start(){
        if (current != null){
            emit(new SessionAlreadyStarted(current));
            throw new AlreadyStartedException(current);
        }
        // https://stackoverflow.com/questions/7348711/recommended-way-to-get-hostname-in-java
        // this discusses why this isn't the most reliable way to obtain hostname; it should suffice for now
        Session newSession = new Session(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(),
            InetAddress.getLocalHost().getHostName()
        );
        repository.insert(SessionData.fromSession(newSession));
        current = newSession;
        emit(new SessionStarted(current));
        return current;
    }
}
