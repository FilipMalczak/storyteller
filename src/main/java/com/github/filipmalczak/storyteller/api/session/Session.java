package com.github.filipmalczak.storyteller.api.session;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.session.events.*;
import com.github.filipmalczak.storyteller.api.tree.TaskResolver;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.valid4j.Assertive.require;

public sealed interface Session<TaskId extends Comparable<TaskId>> permits LiveSession, PastSession {

    String getId();

//todo clean up defaults; maybe smth like getSessionEvent(Class<? extends Joinpoint>), same with getTasks(...)
    default SessionStarted getStartingEvent(){
        var first = getEvents().limit(1).findFirst();
        require(first.isPresent(), "There cannot be a session without at least 'session started' event");
        require(first.get() instanceof SessionStarted, "First event of a session must be 'session started'");
        return (SessionStarted) first.get();
    }

    default ZonedDateTime getStartedAt() {
        return getStartingEvent().getHappenedAt();
    }
    default String getHostname(){
        return getStartingEvent().getHostname();
    }

    default <Ended extends SessionLifecycleEvent & Joinpoint.End> Optional<Ended> getEndingEvent(){
        return getEvents().dropWhile(e -> !(e instanceof  SessionEnded || e instanceof SessionKilled)).findFirst().map(e -> (Ended) e);
        //todo check that there is only one
    }

    default Optional<ZonedDateTime> getEndedAt(){
        return getEndingEvent() .map(SessionEvent::getHappenedAt);
    }

    default Optional<Boolean> isSuccessful(){
        return getEndingEvent().map(e -> e instanceof Joinpoint.End.Success);
    }

    Stream<SessionEvent> getEvents();
    Stream<Pair<TaskId, JournalEntry>> getJournalEntries();

    default Stream<TaskId> getOpenedTasks(){
        return getEvents().filter(x -> x instanceof TaskOpened).map(x -> (TaskOpened<TaskId>) x).map(TaskOpened::getTaskId);
    }

    default Stream<TaskId> getFinishedTasks(){
        return getEvents().filter(x -> x instanceof SessionTaskEvent<?> && x instanceof Joinpoint.End).map(x -> (TaskOpened<TaskId>) x).map(TaskOpened::getTaskId);
    }
}
