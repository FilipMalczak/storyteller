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
}
