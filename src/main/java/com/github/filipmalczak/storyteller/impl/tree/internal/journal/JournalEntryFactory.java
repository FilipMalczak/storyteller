package com.github.filipmalczak.storyteller.impl.tree.internal.journal;

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

    public SubtaskDefined subtaskDefined(@NonNull Task child){
        return new SubtaskDefined(sessionManager.getCurrent(), ZonedDateTime.now(), asList(child));
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

    public BodyChanged bodyChanged(@NonNull List<Task> conflictingTasks){
        //todo require non-empty and non-null?
        return new BodyChanged(sessionManager.getCurrent(), ZonedDateTime.now(), conflictingTasks);
    }

    public BodyExtended bodyExtended(){
        return new BodyExtended(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public SubtaskDisowned subtaskDisowned(@NonNull Task disowned){
        return new SubtaskDisowned(sessionManager.getCurrent(), ZonedDateTime.now(), asList(disowned));
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

    public BodyShrunk bodyShrunk(@NonNull List<Task> disappeared) {
        //todo require non-empty and non-null?
        return new BodyShrunk(sessionManager.getCurrent(), ZonedDateTime.now(), disappeared);
    }

    public TaskAmended taskAmended() {
        return new TaskAmended(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public TaskInterrupted taskInterrupted() {
        return new TaskInterrupted(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public ChoiceWasMade choiceWasMade(Task decision) {
        return new ChoiceWasMade(sessionManager.getCurrent(), ZonedDateTime.now(), asList(decision));
    }

    public SubtaskIncorporated subtaskIncorporated(Task child) {
        return new SubtaskIncorporated(sessionManager.getCurrent(), ZonedDateTime.now(), asList(child));
    }
}
