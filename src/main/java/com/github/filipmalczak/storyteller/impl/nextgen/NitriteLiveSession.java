package com.github.filipmalczak.storyteller.impl.nextgen;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.common.ActionBody;
import com.github.filipmalczak.storyteller.api.common.Factory;
import com.github.filipmalczak.storyteller.api.session.LiveSession;
import com.github.filipmalczak.storyteller.api.session.PastSession;
import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.tree.TaskTreeRoot;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.EventsPersistence;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.dizitart.no2.Nitrite;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NitriteLiveSession<TaskId extends Comparable<TaskId>, Definition, Type extends Enum<Type> & TaskType>
        implements LiveSession<TaskId, Definition, Type, Nitrite> {
    @NonNull @Getter String id;
//    @NonNull ManagedListeners listeners;
//    @NonNull TaskTreeRoot<TaskId, Definition, Type, Nitrite> tree;
//    @NonNull EventsPersistence<TaskId> eventsPersistence;


    @NonNull NitriteTreeConfig<TaskId, Definition, Type> config;
    //todo
    @NonNull Emitter<TaskId> emitter;

    TaskTreeRoot<TaskId, Definition, Type, Nitrite> tree;

    List<SessionEvent> sessionEvents = new ArrayList<>();
    List<Pair<TaskId, JournalEntry>> journalEntries = new ArrayList<>();
    @Getter @NonFinal boolean closed = false;

    public NitriteLiveSession(@NonNull String id,
                              @NonNull NitriteTreeConfig<TaskId, Definition, Type> config,
                              @NonNull Factory<
                                Pair<
                                    NitriteTreeConfig<TaskId, Definition, Type>,
                                >,
                                <TaskId> emitter) {
        this.id = id;
        this.config = config;
        this.emitter = buildEmitter();
        this.tree = buildTree();
    }



    @Override
    public void action(ActionBody<TaskTreeRoot<TaskId, Definition, Type, Nitrite>> body) {
        require(!closed, "Session cannot be closed");
        body.action(tree);
    }

    @Override
    public PastSession<TaskId, Nitrite> forPostMortem() {
        require(closed, "Session must be closed");
        return null;
    }

    @Override
    public void close() {
        if (!closed){
            emitter.ended();
            closed = true;
        }
    }

    @Override
    public Stream<SessionEvent> getEvents() {
        require(!closed, "Session cannot be closed (try using forPostMortem())");
        return sessionEvents.stream();
    }

    @Override
    public Stream<Pair<TaskId, JournalEntry>> getJournalEntries() {
        require(!closed, "Session cannot be closed (try using forPostMortem())");
        return journalEntries.stream();
    }

    public void emit(SessionEvent event){
        eventsPersistence.persist(event);
        listeners.consume(event);
        sessionEvents.add(event);
    }

    public void emit(Task owner, JournalEntry entry){
        eventsPersistence
    }
}
