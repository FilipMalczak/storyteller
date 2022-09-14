package com.github.filipmalczak.storyteller.api.session;

import com.github.filipmalczak.storyteller.api.session.events.*;
import com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import static org.valid4j.Assertive.neverGetHere;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EventType {
    STARTED(SessionStarted.class),
    ENDED(SessionEnded.class),
    KILLED(SessionKilled.class),
    OPENED(TaskOpened.class),
    CLOSED(TaskClosed.class),
    FAILED(TaskFailed.class);

    @Getter
    @NonNull Class<? extends SessionEvent> eventClass;

    public static EventType toType(SessionEvent entry){
        return toType(entry.getClass());
    }

    public static EventType toType(Class<? extends SessionEvent> clazz){
        for (var type: values()){
            if (type.eventClass.equals(clazz))
                return type;
        }
        neverGetHere();
        return null;
    }
}
