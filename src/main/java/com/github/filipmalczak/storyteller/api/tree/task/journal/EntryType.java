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

//todo start using loosened, tightened, reordered, inflated, deflated
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
     * Parallel node depended on incorporation order, but it doesn't anymore. If the
     * node DEFLATED and/or INFLATED, it is recorded before those. Is recorded before ENDED.
     * <p>
     * Owner: parallel node for which the constraint has changed
     */
    ORDERING_LOOSENED(OrderingLoosened.class),
    /**
     * Parallel node didn't depend on incorporation order, but it does now. Is always recorded before AUGMENTED. If the
     * node DEFLATED and/or INFLATED, it is recorded before those. Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Owner: parallel node for which the constraint has changed
     */
    ORDERING_TIGHTENED(OrderingTightened.class),
    /**
     * Parallel node depends (now, not necesseraly in the past) on incorporation order and the order has changed.
     * <p>
     * Note: If there are new incorporated subtasks that are included AFTER the ones that were incorporated in the past,
     * or if the tail of list of subtasks incorporated in the past has changed, it is not emitted. If there are new tasks
     * in between already incorporated ones and if the order of already incorporated ones has changed or if a task "in the middle"
     * has disappeared, then it is recorded. Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Examples when it is emitted:
     * <li> a b -> a c b
     * <li> a b -> b a
     * <li> a b c -> a c
     * <li> b -> b c
     * <p>
     * Examples when it is not emitted:
     * <li> a b -> a b c (this is considered INFLATED)
     * <li> a b c -> a b (this is considered DEFLATED)
     * <li> a b c -> a b d (this is considered DEFLATED and INFLATED)
     * <p>
     * Owner: parallel node that depends on incorporation order
     */
    REORDERED(Reordered.class),
    /**
     * Subtasks that weren't incorporated in the past were incorporated now.
     * <p>
     * Examples when this is emitted:
     * <li> a b -> a b c (no matter if node depends on incorporation order)
     * <li> a b -> a c b (if node doesn't depend on incorporation order)
     * <li> a b -> a c (no matter if node depends on incorporation order)
     * <p>
     * Note: it is possible that a node has been both INFLATED and DEFLATED (see the last example case). In that situation
     * INFLATED is recorded after DEFLATED. Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Owner: parallel node
     */
    INFLATED(Inflated.class),
    /**
     * Subtasks that were incorporated in the past were not incorporated this time.
     * <p>
     * Examples when this is emitted:
     * <li> a b -> a (no matter if node depends on incorporation order)
     * <li> b a -> a (if node doesn't depend on incorporation order)
     * <li> a b c -> a c (if node doesn't depend on incorporation order; if it does, it is REORDERED instead)
     * <li> a b -> a c (no matter if node depends on incorporation order)
     * <p>
     * Note: it is possible that a node has been both INFLATED and DEFLATED (see the last example case). In that situation
     * DEFLATED is recorded before INFLATED . Is recorded before AUGMENTED, thus before ENDED.
     * <p>
     * Owner: parallel node
     */
    DEFLATED(Deflated.class),
    /**
     * The incorporated subtask set or order (only in case of parallel tasks that depend on incorporation order) has
     * changed in any way. That also includes constraints tightening, as in the past there effectively was no order, while
     * there is one now, which is an order change. It is recorded after ORDERING_TIGHTENED, REORDERED, INFLATED and/or
     * DEFLATED, but before ENDED.
     * <p>
     * Owner: parallel node for which the set or order of incorporated subtasks has changed
     */
    AUGMENTED(Augmented.class),
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
