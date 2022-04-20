package com.github.filipmalczak.storyteller.api.stack.task.id;

public interface IdSerializer<Id> {
    String toString(Id id);
}
