package com.github.filipmalczak.storyteller.api.session;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class Session {
    String id;
    ZonedDateTime startedAt;
    String hostname;
}
