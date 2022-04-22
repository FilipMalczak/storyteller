package com.github.filipmalczak.storyteller.api.tree.task.id;

public interface IdSerializer<Id> {
    String toString(Id id);
}
