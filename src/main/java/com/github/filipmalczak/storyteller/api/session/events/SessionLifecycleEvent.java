package com.github.filipmalczak.storyteller.api.session.events;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public sealed abstract class SessionLifecycleEvent implements SessionEvent permits SessionStarted, SessionEnded{
    @NonNull Session subject;
}
