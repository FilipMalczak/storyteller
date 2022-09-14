package com.github.filipmalczak.storyteller.api.session.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public sealed abstract class BaseSessionEvent permits SessionEnded, SessionKilled, SessionStarted {
    @NonNull String sessionId;
    @NonNull ZonedDateTime happenedAt;


}
