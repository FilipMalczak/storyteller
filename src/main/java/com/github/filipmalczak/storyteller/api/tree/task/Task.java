package com.github.filipmalczak.storyteller.api.tree.task;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.util.Optional;
import java.util.stream.Stream;

public interface Task<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>{
    Id getId();
    Definition getDefinition();
    Type getType();

    //todo parenty task, sibling task ? tbd
    Id getParentId();
    Id getPreviousSiblingId();

    JournalEntry record(JournalEntry entry);
    Stream<JournalEntry> getJournalEntries();

    Stream<Id> getSubtaskIds();
    //todo define proper exception protocol for missing subtasks (no such subtask, no such task; no such orphan)
    Optional<Task<Id, Definition, Type>> findSubtask(Id id);
    Stream<Task<Id, Definition, Type>> getSubtasks();

    default Task<Id, Definition, Type> getSubtask(Id id){
        return findSubtask(id).get(); //todo ditto
    }

    Stream<Id> getDisownedSubtaskIds();
    Optional<Task<Id, Definition, Type>> findDisownedSubtask(Id id);
    Stream<Task<Id, Definition, Type>> getDisownedSubtasks();

    default Task<Id, Definition, Type> getDisownedSubtask(Id id){
        return findDisownedSubtask(id).get(); //todo ditto
    }

}
