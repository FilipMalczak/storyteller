package com.github.filipmalczak.storyteller.api.session.events;

import com.github.filipmalczak.storyteller.api.session.Session;

public sealed interface SessionEvent permits SessionStarted, SessionEnded, SessionAlreadyStarted{
    Session getSubject();
}
