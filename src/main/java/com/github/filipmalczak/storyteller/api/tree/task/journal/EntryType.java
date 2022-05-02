package com.github.filipmalczak.storyteller.api.tree.task.journal;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import static org.valid4j.Assertive.neverGetHere;

//todo deppen hierarchy with interfaces:
// JorunalEntry -> (
//  insctructions changed -> (body changed,narrowed,extended),
//  incorporation changed -> (
//      ordering constraints changed -> (tightened, loosened),
//      incorporation order changed -> (reordered, inflated, deflated)
//      ),
//  perform becomes an interface
//  lifecycle event -> (start, end, amend, augmented, perform, caught, interrupted) //maybe amend and augment are structure events?
//  structure event -> (subtask event -> (defined, disowned, incorporated, orphaned) //in the future - adopted
//  )
// at that point add details to refiltered and friends
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EntryType {
    /**
     * Task has just gotten a new subtask.
     * <p>
     * Owner: parent node
     */
    DEFINED(SubtaskDefined.class),
    /**
     * Task has been performed for the first time ever.
     * <p>
     * Owner: performed task
     */
    STARTED(TaskStarted.class),
    /**
     * Tasks instructions have been executed.
     * <p>
     * Owner: performed leaf
     */
    RAN(InstructionsRan.class),
    /**
     * Tasks instructions have been executed in the past, so it is skipped now.
     * <p>
     * Owner: performed leaf
     */
    SKIPPED(InstructionsSkipped.class),
    /**
     * Tasks body have been executed.
     * <p>
     * Owner: performed node
     */
    EXECUTED(BodyExecuted.class),
    /**
     * An exception was thrown when executing a task
     * <p>
     * Owner: task that has directly thrown the exception
     */
    CAUGHT(ExceptionCaught.class),
    /**
     * An exception has been thrown when executing a descendant task.
     * <p>
     * Owner: task that is an ancestor of the one that has thrown the exception.
     */
    INTERRUPTED(TaskInterrupted.class),
    /**
     * Another subtask was expected to happen, which indicates that task body has been redefined.
     * <p>
     * Owner: node that changed
     */
    CHANGED(BodyChanged.class),
    /**
     * Task has already been finished, and yet a new subtask has been defined. Is recorded before DEFINED.
     * <p>
     * Owner: node that got new subtask
     */
    EXTENDED(BodyExtended.class),
    /**
     * More subtasks has been defined, but the task has run its course, which indicates that subtask was
     * removed from the body.
     * <p>
     * Note: Body shrinking isn't treated as amendment, as there is no additional work that needs to happen.
     * <p>
     * Owner: node that has been trimmed
     */
    NARROWED(BodyNarrowed.class),
    /**
     * Task body has already been finished, but it requires some more work. Is recorded after CHANGED or EXTENDED.
     * <p>
     * Owner: node that has been started again
     */
    AMENDED(TaskAmended.class),
    /**
     * A subtask is not part of the task anymore (the task has been changed or narrowed). Is recorded after CHANGED
     * or NARROWED.
     * <p>
     * Owner: parent node
     */
    DISOWNED(SubtaskDisowned.class),
    /**
     * A subtask is not part of the task anymore. Is recorded at the same point in time as DISOWNED.
     * <p>
     * Owner: subtask (node or leaf)
     */
    ORPHANED(TaskOrphaned.class),
    /**
     * List of subtasks incorporated into parallel node has changed in other way than simply growing (which is considered
     * INFLATED) or shrinking (DEFLATED).
     * <p>
     * Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Examples when it is emitted:
     * <li> a b -> a c b
     * <li> a b -> b a
     * <li> a b c -> a c
     * <li> b -> b c
     * <li> a b c -> a b d
     * <p>
     * Examples when it is not emitted:
     * <li> a b -> a b c (this is considered INFLATED)
     * <li> a b c -> a b (this is considered DEFLATED)
     * <p>
     * Owner: parallel node
     */
    REFILTERED(ParallelNodeRefiltered.class),
    /**
     * Tail of the list of the incorporated subtasks has grown.
     * <p>
     * Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Note: If the tail of the incoporated subtasks list has been modified (a subtask isn't incorporated anymore, but a
     * new one is), it is considered REFILTERED.
     * <p>
     * Owner: parallel node
     */
    INFLATED(ParallelNodeInflated.class),
    /**
     * Tail of the list of the incorporated subtasks has grown.
     * <p>
     * Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Note: If the tail of the incorporated subtasks list has been modified (a subtask isn't incorporated anymore, but a
     * new one is), it is considered REFILTERED.
     * <p>
     * Owner: parallel node
     */
    DEFLATED(ParallelNodeDeflated.class),
    /**
     * Set of incorporated subtasks has changed in some way.
     * <p>
     * It is recorded after REWRITTEN, INFLATED or DEFLATED, but before ENDED.
     * <p>
     * Owner: parallel node
     */
    AUGMENTED(ParallelNodeAugmented.class),
    /**
     * Storage state has been fast-forwarded to state after subtask has finished. In other words, subtask has finished
     * performing (executing, running or has been skipped) and further reads from parent task will reflect changes
     * that happened in it (in the subtask).
     * <p>
     * Owner: parent node
     */
    INCORPORATED(SubtaskIncorporated.class),
    /**
     * All the necessary computations of a task were finished. Once this is recorded leaf tasks will be skipped. Between
     * this and AMENDED there can only be SKIPPED, EXECUTED, CHANGED or EXTENDED.
     * <p>
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

    public static boolean referencesSubtask(EntryType entryType){
        return ReferencesSubtask.class.isAssignableFrom(entryType.entryClass);
    }

    public static boolean referencesSubtasks(EntryType entryType){
        return ReferencesSubtasks.class.isAssignableFrom(entryType.entryClass);
    }
}
