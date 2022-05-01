package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
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

import static java.util.Arrays.asList;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JournalEntryFactory {
    SessionManager sessionManager;


    public <Id extends Comparable<Id>> SubtaskDefined subtaskDefined(@NonNull Id child){
        return new SubtaskDefined(sessionManager.getCurrent(), ZonedDateTime.now(), child);
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

    public <Id extends Comparable<Id>> BodyChanged bodyChanged(@NonNull List<Id> conflictingTasks){
        //todo require non-empty and non-null?
        return new BodyChanged(sessionManager.getCurrent(), ZonedDateTime.now(), conflictingTasks);
    }

    public BodyExtended bodyExtended(){
        return new BodyExtended(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public <Id extends Comparable<Id>> SubtaskDisowned subtaskDisowned(@NonNull Id disowned){
        return new SubtaskDisowned(sessionManager.getCurrent(), ZonedDateTime.now(), disowned);
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

    public <Id extends Comparable<Id>> BodyNarrowed bodyShrunk(@NonNull List<Id> disappeared) {
        //todo require non-empty and non-null?
        return new BodyNarrowed(sessionManager.getCurrent(), ZonedDateTime.now(), disappeared);
    }

    public TaskAmended taskAmended() {
        return new TaskAmended(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public TaskInterrupted taskInterrupted() {
        return new TaskInterrupted(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public SubtaskIncorporated subtaskIncorporated(Task child) {
        return new SubtaskIncorporated(sessionManager.getCurrent(), ZonedDateTime.now(), child.getId());
    }
}
