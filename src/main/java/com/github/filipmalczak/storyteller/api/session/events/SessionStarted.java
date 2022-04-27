package com.github.filipmalczak.storyteller.api.session.events;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Value
public class SessionStarted implements SessionEvent {
    @NonNull Session subject;
}
