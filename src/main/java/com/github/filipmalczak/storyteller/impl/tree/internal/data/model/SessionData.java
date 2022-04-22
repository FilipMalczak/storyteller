package com.github.filipmalczak.storyteller.impl.tree.internal.data.model;

import com.github.filipmalczak.storyteller.api.tree.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionData {
    @Id
    String id;
    ZonedDateTime startedAt;
    String hostname;

    public Session toSession(){
        return new Session(id, startedAt, hostname);
    }

    public static SessionData fromSession(Session session){
        return new SessionData(session.getId(), session.getStartedAt(), session.getHostname());
    }
}
