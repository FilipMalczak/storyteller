package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.impl.stack.ExecutionFriend;
import com.github.filipmalczak.storyteller.impl.stack.SubtaskOrderingStrategy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.valid4j.Assertive.require;

@Flogger
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class LinearSubtaskOrderingStrategy<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements SubtaskOrderingStrategy<Id> {
    final List<Id> expectations;
    @Setter ExecutionFriend<Id, Definition, Type> friend;

    @Override
    public boolean hasExpectations() {
        return !expectations.isEmpty();
    }

    @Override
    public Collection<Id> getCandidatesForReusing() {
        return asList(expectations.get(0));
    }

    @Override
    public Id reuse(Id id) {
        var reused = expectations.remove(0);
        require(id, equalTo(reused));
        return id;
    }

    @Override
    public void onNoReusable(Collection<Id> candidates) {
        //we can assume that, because getCandidatesForReusing will always remove 1-element collection
        var expected = candidates.stream().findFirst().get();
        var conflicting = friend.findTask(expected);
        require(conflicting.isPresent(), "Non-reusable ID must refer to an existing task");
        var conflictingTask = conflicting.get();
        var def = friend.idGenerator().definition();
        log.atFine().log("Conflicting subtask of task %s: definition was %s, but is now %s", friend.parentId(), conflictingTask.getDefinition(), def);
        require(conflictingTask.getDefinition(), not(equalTo(def)));
        friend.recordInParent(friend.journalEntryFactory().bodyChanged(conflictingTask));
        friend.disownExpectedUpTheTrace();
        friend.setId(friend.idGenerator().generate());
        log.atFine().log("Recovered from conflict; generated new ID: %s", expected);
    }
}