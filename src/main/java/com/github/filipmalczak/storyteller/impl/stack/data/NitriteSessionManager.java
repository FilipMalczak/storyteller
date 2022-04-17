package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.api.stack.Session;
import com.github.filipmalczak.storyteller.impl.stack.data.model.SessionData;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.dizitart.no2.objects.ObjectRepository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.valid4j.Assertive.require;

public class NitriteSessionManager implements SessionManager {
    @Setter(AccessLevel.PACKAGE) @NonNull ObjectRepository<SessionData> repository;
    Session current;

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
            start();
        }
        return current;
    }

    private void start(){
        Session newSession = new Session(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(),
            "hostname" //todo
        );
        repository.insert(SessionData.fromSession(newSession));
        current = newSession;
    }
}
