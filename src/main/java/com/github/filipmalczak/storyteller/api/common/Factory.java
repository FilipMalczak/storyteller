package com.github.filipmalczak.storyteller.api.common;

public interface Factory<Config, Produced> {
    Produced create(Config config);
}
