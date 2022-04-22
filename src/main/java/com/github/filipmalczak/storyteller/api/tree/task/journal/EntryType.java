package com.github.filipmalczak.storyteller.api.tree.task.journal;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import static org.valid4j.Assertive.neverGetHere;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntryType {
    /**
     * Task has just gotten a new subtask.
     * Owner: parent task (non-leaf)
     */
    DEFINED(SubtaskDefined.class),
    /**
     * Task has been performed for the first time ever.
     * Owner: performed task (any)
     */
    STARTED(TaskStarted.class),
    /**
     * Tasks instructions have been executed.
     * Owner: performed task (leaf)
     */
    RAN(InstructionsRan.class),
    /**
     * Tasks instructions have been executed in the past, so it is skipped now.
     * Owner: performed task (leaf)
     */
    SKIPPED(InstructionsSkipped.class),
    /**
     * Tasks body have been executed.
     * Owner: performed task (non-leaf)
     */
    EXECUTED(BodyExecuted.class), //todo
    /**
     * An exception was thrown when executing a task
     * Owner: task that has directly thrown the exception (any)
     */
    CAUGHT(ExceptionCaught.class),
    /**
     * An exception has been thrown when executing a descendant task.
     * Owner: task that is an ancestor of the one that has thrown the exception.
     */
    INTERRUPTED(TaskInterrupted.class), //todo
    /**
     * Another subtask was expected to happen, which indicates that task body has been redefined.
     * Owner: the task that changed (non-leaf)
     */
    CHANGED(BodyChanged.class),
    /**
     * Task has already been finished, and yet a new subtask has been defined. Is recorded before DEFINED.
     * Owner: the task that got new subtask (non-leaf)
     */
    EXTENDED(BodyExtended.class),
    /**
     * More subtasks has been defined, but the task has run its course, which indicates that subtask was
     * removed from the body.
     * Owner: the task that has been trimmed (non-leaf)
     */
    SHRUNK(BodyShrunk.class), //todo
    /**
     * Task body has already been finished, but it requires some more work. Is recorded after CHANGED or EXTENDED.
     * Owner: the task that has been started again (non-leaf)
     */
    AMENDED(TaskAmended.class), //todo
    /**
     * A subtask is not part of the task anymore (the task has been changed or shrunk). Is recorded after CHANGED
     * or SHRUNK.
     * Owner: parent task (non-leaf)
     */
    DISOWNED(SubtaskDisowned.class),
    /**
     * A subtask is not part of the task anymore. Is recorded at the same point in time as DISOWNED.
     * Owner: child task (non-root)
     */
    ORPHANED(TaskOrphaned.class),
    /**
     * All subtask of choice has finished, insights were analysed and decision on how to proceed has been made.
     * Owner: performed task (choice)
     */
    DECIDED(ChoiceWasMade.class), //todo
    /**
     * Storage state has been fast-forwarded to state after subtask has finished. In other words, subtask has finished
     * performing (executing, running or has been skipped) and further reads from parent task will reflect changes
     * that happened in it (in the subtask). In case of choice tasks is recorded after DECIDED.
     * Owner: parent task (non-leaf)
     */
    INCORPORATED(SubtaskIncorporated.class), //todo
    /**
     * All the necessary computations of a task were finished. Once this is recorded leaf tasks will be skipped. Between
     * this and AMENDED there can only be SKIPPED, EXECUTED, CHANGED or EXTENDED.
     * Owner: performed task
     */
    ENDED(TaskEnded.class),;

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
        return entryType == CAUGHT;
    }

    public static boolean referencesSubtasks(EntryType entryType){
        return ReferencesSubtask.class.isAssignableFrom(entryType.entryClass);
    }
}
