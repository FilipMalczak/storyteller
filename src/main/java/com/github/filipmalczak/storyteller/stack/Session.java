package com.github.filipmalczak.storyteller.stack;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class Session {
    String id;
    ZonedDateTime startedAt;
    String hostname;
}
