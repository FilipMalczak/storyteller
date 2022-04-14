package com.github.filipmalczak.storyteller.impl.stack.data.model;

import com.github.filipmalczak.storyteller.stack.task.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskData<TaskId, Definition, Type extends Enum<Type> & TaskType> {
    @Id
    TaskId id;
    Definition definition;
    Type type;
    List<TaskId> subtasks;
}
