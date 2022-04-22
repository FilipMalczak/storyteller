package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.Session;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.SessionData;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.objects.ObjectRepository;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.valid4j.Assertive.require;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
            return start();
        }
        return current;
    }

    @Override
    public void end() {

    }

    @SneakyThrows
    public Session start(){
        if (current != null){
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
        return current;
    }
}
