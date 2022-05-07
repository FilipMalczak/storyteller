package com.github.filipmalczak.storyteller.api.tree.task;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskEnded;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskStarted;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType.impactsFinishedStateOfTask;
import static com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType.toType;

public interface Task<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>{
    Id getId();
    Definition getDefinition();
    Type getType();

    //todo parenty task, sibling task ? tbd
    //todo remove these
//    Id getParentId();
//    Id getPreviousSiblingId();

    JournalEntry record(JournalEntry entry);
    Stream<JournalEntry> getJournalEntries();

    Stream<Id> getSubtaskIds();
    //todo define proper exception protocol for missing subtasks (no such subtask, no such task; no such orphan)
    Optional<Task<Id, Definition, Type>> findSubtask(Id id);
    Stream<Task<Id, Definition, Type>> getSubtasks(Stream<Id> ids);

    default Stream<Task<Id, Definition, Type>> getSubtasks(){
        return getSubtasks(getSubtaskIds());
    }

    default Task<Id, Definition, Type> getSubtask(Id id){
        return findSubtask(id).get(); //todo ditto
    }

    Stream<Id> getDisownedSubtaskIds();
    Optional<Task<Id, Definition, Type>> findDisownedSubtask(Id id);
    Stream<Task<Id, Definition, Type>> getDisownedSubtasks();

    default Task<Id, Definition, Type> getDisownedSubtask(Id id){
        return findDisownedSubtask(id).get(); //todo ditto
    }

    default boolean isStarted(){
        return getJournalEntries().findFirst().filter(e -> e instanceof TaskStarted).isPresent();
    }

    default boolean isFinished(){
        return getJournalEntries()
            .filter(e -> impactsFinishedStateOfTask(toType(e)))
            .reduce((e1, e2) -> e2) //ugly way to say findLast
            .map(e -> e instanceof TaskEnded)
            .orElse(false);
    }
}
