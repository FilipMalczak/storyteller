package com.github.filipmalczak.storyteller.api.tree.task;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskAmended;
import lombok.Value;

@Value
public class TaskSpec<Definition, Type extends Enum<Type> & TaskType> {
    Definition definition;
    Type type;

    public static <Definition, Type extends Enum<Type> & TaskType> TaskSpec<Definition, Type> of(Definition definition, Type type){
        return new TaskSpec<>(definition, type);
    }
}
