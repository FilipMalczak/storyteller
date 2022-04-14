package com.github.filipmalczak.storyteller.stack.task.journal;

import com.github.filipmalczak.storyteller.stack.task.journal.entries.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import static org.valid4j.Assertive.neverGetHere;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntryType {
    DEFINE(DefineSubtask.class),
    START(StartTask.class),
    RUN(RunIntructions.class),
    SKIP(SkipAlreadyExecuted.class),
    CATCH(CatchException.class),
    NODE_EXTENDED(NodeExtended.class),
    END(EndTask.class),
    INTEGRATE(IntegrateSubtask.class);

    @Getter Class<? extends JournalEntry> entryClass;

    public static EntryType toType(JournalEntry entry){
        return toType(entry.getClass());
    }

    public static EntryType toType(Class<? extends JournalEntry> clazz){
        for (var type: values()){
            if (type.entryClass.equals(clazz))
                return type;
        }
        neverGetHere();
        return null;
    }
}
