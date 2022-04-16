package com.github.filipmalczak.storyteller.impl.storage.config;

public interface IdSerializer<Id> {
    String toString(Id id);
}
