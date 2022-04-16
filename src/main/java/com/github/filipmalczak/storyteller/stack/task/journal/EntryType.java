package com.github.filipmalczak.storyteller.stack.task.journal;

import com.github.filipmalczak.storyteller.stack.task.journal.entries.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import static org.valid4j.Assertive.neverGetHere;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntryType {
    DEFINE(SubtaskDefined.class),
    START(TaskStarted.class),
    RUN(InstructionsRan.class),
    SKIP(InstructionsSkipped.class),
    CATCH(ExceptionCaught.class),
    EXTEND(NodeExtended.class),
    CHANGE(BodyChanged.class),
    DISOWN(SubtaskDisowned.class),
    ORPHANED(DisownedByParent.class),
    END(TaskEnded.class),;

    @Getter @NonNull Class<? extends JournalEntry> entryClass;

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

    public static boolean describesException(EntryType entryType){
        return entryType == CATCH;
    }

    public static boolean referencesSubtask(EntryType entryType){
        return ReferencesSubtask.class.isAssignableFrom(entryType.entryClass);
    }
}
