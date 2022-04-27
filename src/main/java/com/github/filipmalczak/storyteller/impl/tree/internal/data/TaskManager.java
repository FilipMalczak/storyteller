package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

import java.util.Optional;

public interface TaskManager<Id, Definition, Type extends Enum<Type> & TaskType> {
    Optional<Task<Id, Definition, Type>> findById(Id id);

    default Task<Id, Definition, Type> getById(Id id){
        return findById(id).get(); //todo better exception
    }

    void register(Task<Id, Definition, Type> task);

    void update(Task<Id, Definition, Type> task);

    //todo findOrphans and findOrphansOf (tbd; seem valid, but there is no use for them at this point)


}
