package com.github.filipmalczak.storyteller.impl.tree.internal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.Events;

import java.util.Optional;

/**
 * Not a nicest way to do it, but it exposes some details of the tree itself to execution classes.
 */
public interface ExecutionFriend<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    void disownExpectedUpTheTrace();
    void setId(Id id);
    IdGenerator<Id, Definition, Type> idGenerator();
    Optional<Task<Id, Definition, Type>> findTask(Id id);
//    Task<Id, Definition, Type> thisTask(); //todo cleanup
    Id parentId();
    Events<Id> events();

}
