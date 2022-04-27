package com.github.filipmalczak.storyteller.impl.tree.internal.order;

import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.impl.tree.internal.ExecutionFriend;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.util.Collection;
import java.util.List;

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
    @Getter List<Id> conflicts = null;

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
        //todo confirm that bodyChanged is emitted in correct place
        conflicts = candidates.stream().toList();
        friend.disownExpectedUpTheTrace();
        friend.setId(friend.idGenerator().generate());
        log.atFine().log("Recovered from conflict; generated new ID: %s", expected);
    }
}
