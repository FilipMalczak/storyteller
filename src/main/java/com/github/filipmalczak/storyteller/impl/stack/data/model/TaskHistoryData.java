package com.github.filipmalczak.storyteller.impl.stack.data.model;

import com.github.filipmalczak.storyteller.stack.task.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskHistoryData<TaskId extends Comparable<TaskId>> {
    @Id
    TaskId id;
    String sessionId;
    List<TaskId> history;
}
