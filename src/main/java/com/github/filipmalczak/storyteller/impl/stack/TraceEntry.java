package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.stack.task.Task;
import com.github.filipmalczak.storyteller.stack.task.TaskType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Deque;
import java.util.LinkedList;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TraceEntry<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> {
    Task<Id, Definition, Type> executedTask;
    final Deque<Id> expectedSubtaskIds; //cannot simply be queue, because in case of conflict we need to push back to the front
    //fixme could be fixed by using peek() and popping only if no conflict occurs

    public TraceEntry(Deque<Id> expectedSubtaskIds) {
        this.expectedSubtaskIds = expectedSubtaskIds;
    }

    public TraceEntry() {
        this(new LinkedList<>());
    }
}
