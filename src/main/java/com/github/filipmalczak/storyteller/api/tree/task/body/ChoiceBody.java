package com.github.filipmalczak.storyteller.api.tree.task.body;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;

@FunctionalInterface
public interface ChoiceBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {

    //todo rethink these; once you do, add toBeContinued feature too
    class SubtaskNotFoundException extends RuntimeException {
        //todo
    }

    class CannotChooseException extends RuntimeException {
        //todo
    }

    class CannotMakeChoiceException extends CannotChooseException {
        //todo should add definition of the choice that throws CannotChooseException
    }

    default Task<Id, Definition, Type> cannotMakeChoice(){
        throw new CannotChooseException(); //todo
    }

    Task<Id, Definition, Type> makeChoice(TaskTree<Id, Definition, Type, NoSql> executor, ReadStorage<NoSql> storage);
}
