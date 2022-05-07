package com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization;

import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.JournalEntryManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.TaskManager;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.TaskData;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;

@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSerializer<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>  {
    @NonNull JournalEntryManager<Id> journalEntryManager;
    @NonNull TaskManager<Id, Definition, Type> taskManager;

    public Task<Id, Definition, Type> toTask(TaskData<Id, Definition, Type> data){
        return SimpleTask.<Id, Definition, Type>builder()
            .id(data.getId())
            .definition(data.getDefinition())
            .type(data.getType())
//            .parentId(data.getParentId())
//            .previousSiblingId(data.getPreviousSiblingId())
            .journal(new LinkedList<>(journalEntryManager.findById(data.getId()).toList()))
            .taskResolver(taskManager)
            .build();
    }

    public TaskData<Id, Definition, Type> fromTask(Task<Id, Definition, Type> task){
        return new TaskData<>(
            task.getId(),
            task.getDefinition(),
            task.getType()
//            task.getParentId(),
//            task.getPreviousSiblingId()
        );
    }
}
