package com.github.filipmalczak.storyteller.api.session.events;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.NonNull;
import lombok.Value;

@Value
public class SessionAlreadyStarted implements SessionEvent{
    @NonNull Session subject;
}
