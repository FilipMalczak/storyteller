package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.SessionManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

//todo this can be moved to API as soon as I replace session manager with Sessions
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JournalEntryFactory {
    SessionManager sessionManager;


    public <Id extends Comparable<Id>> SubtaskDefined<Id> subtaskDefined(@NonNull Id child){
        return new SubtaskDefined<>(sessionManager.getCurrent(), ZonedDateTime.now(), child);
    }

    public TaskStarted taskStarted(){
        return new TaskStarted(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public InstructionsRan instructionsRan(){
        return new InstructionsRan(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public InstructionsSkipped instructionsSkipped(){
        return new InstructionsSkipped(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public <Id extends Comparable<Id>> BodyChanged<Id> bodyChanged(@NonNull Id pivot, @NonNull List<Id> conflictingTasks){
        //todo require non-empty and non-null?
        return new BodyChanged<>(sessionManager.getCurrent(), ZonedDateTime.now(), pivot, conflictingTasks);
    }

    public <Id extends Comparable<Id>> BodyExtended<Id> bodyExtended(@NonNull List<Id> added){
        return new BodyExtended<>(sessionManager.getCurrent(), ZonedDateTime.now(), added);
    }

    public <Id extends Comparable<Id>> SubtaskDisowned<Id> subtaskDisowned(@NonNull Id disowned){
        return new SubtaskDisowned<>(sessionManager.getCurrent(), ZonedDateTime.now(), disowned);
    }

    public TaskOrphaned taskOrphaned(){
        return new TaskOrphaned(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    @SneakyThrows
    public ExceptionCaught exceptionCaught(@NonNull Exception e){
        String trace;
        try (var sw = new StringWriter(); var writer = new PrintWriter(sw)) {
            e.printStackTrace(writer);
            trace = sw.toString();
        }

        return new ExceptionCaught(
            sessionManager.getCurrent(), ZonedDateTime.now(),
            e.getClass().getCanonicalName(),
            e.getMessage(),
            trace
        );
    }

    public TaskEnded taskEnded(){
        return new TaskEnded(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public BodyExecuted bodyExecuted() {
        return new BodyExecuted(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public <Id extends Comparable<Id>> BodyNarrowed<Id> bodyNarrowed(@NonNull List<Id> disappeared) {
        //todo require non-empty and non-null?
        return new BodyNarrowed<>(sessionManager.getCurrent(), ZonedDateTime.now(), disappeared);
    }

    public TaskAmended taskAmended() {
        return new TaskAmended(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public <Id extends Comparable<Id>> ParallelNodeDeflated<Id> nodeDeflated(@NonNull Set<Id> disappeared){
        return new ParallelNodeDeflated<>(sessionManager.getCurrent(), ZonedDateTime.now(), disappeared.stream().toList());
    }

    public <Id extends Comparable<Id>> ParallelNodeInflated<Id> nodeInflated(@NonNull Set<Id> appeared){
        return new ParallelNodeInflated<>(sessionManager.getCurrent(), ZonedDateTime.now(), appeared.stream().toList());
    }

    public <Id extends Comparable<Id>> ParallelNodeRefiltered<Id> nodeRefiltered(@NonNull Set<Id> appeared, @NonNull Set<Id> disappeared){
        return new ParallelNodeRefiltered<>(sessionManager.getCurrent(), ZonedDateTime.now(), appeared.stream().toList(), disappeared.stream().toList());
    }

    public ParallelNodeAugmented nodeAugmented(){
        return new ParallelNodeAugmented(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public TaskInterrupted taskInterrupted() {
        return new TaskInterrupted(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public <Id extends  Comparable<Id>> SubtaskIncorporated<Id> subtaskIncorporated(Id child) {
        return new SubtaskIncorporated<>(sessionManager.getCurrent(), ZonedDateTime.now(), child);
    }
}
