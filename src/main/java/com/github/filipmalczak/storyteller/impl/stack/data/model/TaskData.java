package com.github.filipmalczak.storyteller.impl.stack.data.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.filipmalczak.storyteller.stack.task.TaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskData<TaskId extends Comparable<TaskId>, Definition, Type extends Enum<Type> & TaskType> {
    @Id
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    TaskId id;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    Definition definition;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    Type type;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
    List<TaskId> subtasks;
}
