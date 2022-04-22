package com.github.filipmalczak.storyteller.api.stack.task.body;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.storage.ReadStorage;

import java.util.function.Function;

//todo once we rename to ...tree, this should become ParallelNodeBody (once it has integrate param too)
public interface ChoiceBody<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
    interface Insight<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, NoSql> {
        ReadStorage<NoSql> into(Id id);

        default ReadStorage<NoSql> into(Task<Id, Definition, Type> subtask){
            return into(subtask.getId());
        }
    }

    //todo rethink these; once you do, ass toBeContinued feature too<
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

    Task<Id, Definition, Type> makeChoice(StackedExecutor<Id, Definition, Type, NoSql> executor, ReadStorage<NoSql> storage, Insight<Id, Definition, Type, NoSql> insights);
}
