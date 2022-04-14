package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.stack.Session;

import java.util.Optional;

public interface SessionManager {
    Optional<Session> findById(String id);

    default Session getById(String id){
        return findById(id).get(); //todo exception
    }

    Session getCurrent();
}
