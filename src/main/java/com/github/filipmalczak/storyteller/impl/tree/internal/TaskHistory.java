package com.github.filipmalczak.storyteller.impl.tree.internal;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskHistory<Id> {
    //fields are conretely typed to allow pushing in front
    LinkedList<Id> full = new LinkedList<>();
    LinkedList<Id> leaves = new LinkedList<>();

    public void add(Id id, boolean isLeaf){
        full.addFirst(id);
        if (isLeaf){
            leaves.addFirst(id);
        }
    }

    public TaskHistory<Id> copy(){
        return new TaskHistory<>(new LinkedList<>(full), new LinkedList<>(leaves));
    }

    public List<Id> getFull() {
        return full;
    }

    public List<Id> getLeaves() {
        return leaves;
    }
}
