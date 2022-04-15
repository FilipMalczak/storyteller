package com.github.filipmalczak.storyteller.impl.stack.data.serialization;

import com.github.filipmalczak.storyteller.impl.stack.data.JournalEntryManager;
import com.github.filipmalczak.storyteller.impl.stack.data.TaskManager;
import com.github.filipmalczak.storyteller.impl.stack.data.model.TaskData;
import com.github.filipmalczak.storyteller.stack.task.Task;
import com.github.filipmalczak.storyteller.stack.task.TaskType;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSerializer<TaskId extends Comparable<TaskId>, Definition, Type extends Enum<Type> & TaskType>  {
    @NonNull JournalEntryManager<TaskId> journalEntryManager;
    @NonNull TaskManager<TaskId, Definition, Type> taskManager;

    public Task<TaskId, Definition, Type> toTask(TaskData<TaskId, Definition, Type> data){
        return Task.<TaskId, Definition, Type>builder()
            .id(data.getId())
            .definition(data.getDefinition())
            .type(data.getType())
            .subtasks(data.getSubtasks().stream().map(taskManager::getById).toList())
            .journal(journalEntryManager.findByTaskId(data.getId()).toList())
            .build();
    }

    public TaskData<TaskId, Definition, Type> fromTask(Task<TaskId, Definition, Type> task){
        return new TaskData<>(
            task.getId(),
            task.getDefinition(),
            task.getType(),
            task.getSubtasks().stream().map(Task::getId).toList()
        );
    }
}
