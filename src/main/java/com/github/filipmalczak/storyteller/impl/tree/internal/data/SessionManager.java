package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.Session;
import com.github.filipmalczak.storyteller.api.tree.Sessions;

import java.util.Optional;

public interface SessionManager extends Sessions {
    Optional<Session> findById(String id);

    default Session getById(String id){
        return findById(id).get(); //todo exception
    }

    Session getCurrent();

    //fixme without start/close called at root level running the same story twice in a single runtime will result in single session
}
