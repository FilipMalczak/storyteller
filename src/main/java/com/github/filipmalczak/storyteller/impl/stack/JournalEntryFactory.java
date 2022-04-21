package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.stack.data.SessionManager;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JournalEntryFactory {
    SessionManager sessionManager;

    public SubtaskDefined subtaskDefined(Task child){
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

    public BodyChanged bodyChanged(Task conflictingTask){
        return new BodyChanged(sessionManager.getCurrent(), ZonedDateTime.now(), conflictingTask);
    }

    public NodeExtended nodeExtended(){
        return new NodeExtended(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    public SubtaskDisowned subtaskDisowned(Task disowned){
        return new SubtaskDisowned(sessionManager.getCurrent(), ZonedDateTime.now(), disowned);
    }

    public DisownedByParent disownedByParent(){
        return new DisownedByParent(sessionManager.getCurrent(), ZonedDateTime.now());
    }

    @SneakyThrows
    public ExceptionCaught exceptionCaught(Exception e){
        String trace;
        try (var sw = new StringWriter(); var writer = new PrintWriter(sw)) {
            e.printStackTrace(writer);
            trace = sw.toString();
        }

        //todo extract stack trace to string
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
}
