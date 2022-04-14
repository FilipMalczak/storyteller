package com.github.filipmalczak.storyteller.impl.storage;

public interface IdSerializer<Id> {
    String toString(Id id);
}
