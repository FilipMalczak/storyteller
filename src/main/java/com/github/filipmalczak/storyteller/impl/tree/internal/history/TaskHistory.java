package com.github.filipmalczak.storyteller.impl.tree.internal.history;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.LinkedList;
import java.util.List;

@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TaskHistory<Id> {
    //fields are conretely typed to allow pushing in front
    @NonNull List<Id> full;
    @NonNull List<Id> writers;

    public void add(Id id, boolean isWriting){
        full.add(0, id);
        if (isWriting){
            writers.add(0, id);
        }
    }

    public TaskHistory<Id> copy(){
        return new TaskHistory<>(new LinkedList<>(full), new LinkedList<>(writers));
    }

    public static <Id> TaskHistory<Id> empty(){
        return new TaskHistory<>(new LinkedList<>(), new LinkedList<>());
    }
}
