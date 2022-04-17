package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.TaskBody;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadWriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.*;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.reverse;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.valid4j.Assertive.neverGetHere;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Flogger
public class NitriteStackedExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements StackedExecutor<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idIdGeneratorFactory;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace; // trace[0] - parent; trace[-1] - root; empty for root

    private Optional<Deque<Id>> getExpected(){
        return trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds);
    }

    private Optional<Task<Id, Definition, Type>> getParent(){
        return trace.stream().findFirst().map(TraceEntry::getExecutedTask);
    }

    @Override
    public Task<Id, Definition, Type> executeTask(Definition definition, Type type, TaskBody<Id, Definition, Type, Nitrite> body) {
        log.atFine().log("Executing '%s' of type %s", definition, type);
        var expected = getExpected();
        var parent = getParent();
        log.atFine().log("Known subtasks of parent: %s", expected);
        var generator = idIdGeneratorFactory.over(definition, type);
        Id id;
        if (type.isRoot()) {
            require(trace.isEmpty(), "Root task needs to be executed without any tasks at the stack");
            require(parent.isEmpty(), "Root task cannot have a parent");
            require(expected.isEmpty(), "Root task cannot have expected ID");
            require(!type.isLeaf(), "Root task cannot be a leaf task as well");
            require(body instanceof NodeBody, "Root task body must be implemented as %1", NodeBody.class.getCanonicalName());
        } else {
            require(!trace.isEmpty(), "Non-root task needs to be executed with at least one task at the stack");
            require(parent.isPresent(), "Non-root task must have a parent");
            if (type.isLeaf()) {
                require(body instanceof LeafBody, "Leaf task body must be implemented as %1", LeafBody.class.getCanonicalName());
            } else {
                require(body instanceof NodeBody, "Node task body must be implemented as %1", NodeBody.class.getCanonicalName());
            }
        }
        boolean isDefined = false;
        if (expected.isEmpty() || expected.get().isEmpty()) {
            id = generator.generate();
            log.atFine().log("Task wasn't defined yet; generated ID: %s", id);
            if (parent.isPresent() && isFinished(parent.get())){
                log.atFine().log("Parent was finished; extending parent");
                extend(parent.get());
                disownExpectedUpTheTrace();
            }
        } else {
            var expectations = expected.get();
            //we pop it here
            id = expectations.removeFirst();
            if (generator.canReuse(id)){
                isDefined = true;
                log.atFine().log("Reusing ID of already defined task: %s", id);
            } else {
                //so in case of conflict
                var conflicting = managers.getTaskManager().findById(id);
                require(conflicting.isPresent(), "Non-reusable ID must refer to an existing task");
                var conflictingTask = conflicting.get();
                log.atFine().log("Conflicting subtask of task %s: definition was %s, but is now %s", parent.get().getId(), conflictingTask.getDefinition(), definition);
                require(conflictingTask.getDefinition(), not(equalTo(definition)));
                recordConflictInParent(parent.get(), conflictingTask);
                //we need to push it back to top
                expectations.addFirst(id);
                //so it can be disowned in correct order
                disownExpectedUpTheTrace();
                id = generator.generate();
                log.atFine().log("Recovered from conflict; generated new ID: %s", id);
            }
        }
        final var finalId = id;
        var found = managers.getTaskManager().findById(id);
        log.atFine().log("Retrieved task: %s", found);
        var task = found
            .orElseGet(
                () -> Task.<Id, Definition, Type>builder()
                    .id(finalId)
                    .definition(definition)
                    .type(type)
                    .build()
            );
        if (found.isPresent()) {
            require(task.getDefinition(), equalTo(definition));
            require(task.getType(), equalTo(type));
        } else {
            managers.getTaskManager().register(task);
        }
        if (parent.isPresent() && !isDefined){
            defineInParent(parent.get(), task);
        }
        boolean isLeaf = type.isLeaf();
        boolean finished = isFinished(task);
        boolean skip = isLeaf && finished;
        if (!isStarted(task)){
            start(task);
        }
        history.start(id, parent.map(Task::getId));
        if (!skip) {
            runBody(task, body);
            finished = isFinished(task);
        } else {
            recordSkipping(task);
        }
        for (var traceEntry: trace){
            history.add(traceEntry.getExecutedTask().getId(), id, isLeaf);
        }
        if (!finished) {
            end(task);
        }
        return task;
    }

    private boolean isStarted(Task task){
        var firstJournalEntry = task.getJournalEntries().findFirst();
        if (firstJournalEntry.isEmpty())
            return false;
        require(firstJournalEntry.get() instanceof TaskStarted, "First journal entry must describe starting the task");
        return true;
    }

    private boolean isFinished(Task task){
        var entries = new ArrayList<>(task.getJournalEntries().toList());
        if (entries.isEmpty())
            return false;
        reverse(entries);
        var firstNonSkip = entries.stream().dropWhile(e -> e instanceof InstructionsSkipped).findFirst();
        require(firstNonSkip.isPresent(), "Journal has to consist of something else than just 'skip' records");
        return firstNonSkip.get() instanceof TaskEnded;
    }

    private void extend(Task task){
        var entry = task.record(new NodeExtended(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void start(Task task){
        var entry = task.record(new TaskStarted(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @ToString
    private static class AlreadyRecordedException extends RuntimeException {
        @NonNull Exception alreadyRecorded;

        public AlreadyRecordedException(@NonNull Exception alreadyRecorded) {
            super(alreadyRecorded);
            this.alreadyRecorded = alreadyRecorded;
        }
    }

    @SneakyThrows
    private void runBody(Task<Id, Definition, Type> task, TaskBody<Id, Definition, Type, Nitrite> body){
        //todo missing define and integrate; are they still needed though?
        recordRunning(task);
        try {
            if (body instanceof NodeBody)
                runNode(task, (NodeBody<Id, Definition, Type, Nitrite>) body);
            else if (body instanceof LeafBody)
                runLeaf(task, (LeafBody<Id, Definition, Type, Nitrite>) body);
            else
                neverGetHere(); //todo this should be coverable with type system
        } catch (Exception e) {
            if (e instanceof AlreadyRecordedException){
                if (task.getType().isRoot()){
                    throw ((AlreadyRecordedException) e).alreadyRecorded;
                }
            } else {
                recordException(task, e);
                throw new AlreadyRecordedException(e);
            }
        }
    }

    private void runNode(Task<Id, Definition, Type> task, NodeBody<Id, Definition, Type, Nitrite> body){
        var newTrace = new LinkedList<>(trace);
        var newEntry = new TraceEntry<>(task, new LinkedList<>(task.getSubtasks().stream().map(Task::getId).toList()));
        newTrace.addFirst(newEntry);
        body.perform(
            new NitriteStackedExecutor<>(
                managers,
                history,
                storageConfig,
                idIdGeneratorFactory,
                newTrace
            ),
            new NitriteReadStorage(storageConfig, history, task.getId())
        );
    }

    private void runLeaf(Task<Id, Definition, Type> task, LeafBody<Id, Definition, Type, Nitrite> body){
        var storage = new NitriteReadWriteStorage(storageConfig, history, task.getId());
        try {
            body.perform(storage);
            storage.flush();
        } catch (Exception e){
            storage.purge();
        }
    }

    private void recordException(Task task, Exception e){
        //todo extract stack trace to string
        //todo prepare journal entry factory
        var entry = task.record(new ExceptionCaught(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), e.getMessage(), ""));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void recordRunning(Task task){
        var entry = task.record(new InstructionsRan(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void recordSkipping(Task task){
        var entry = task.record(new InstructionsSkipped(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void end(Task task){
        var entry = task.record(new TaskEnded(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void defineInParent(Task<Id, Definition, Type> parent, Task child){
        parent.getSubtasks().add(child);
        var entry = parent.record(new SubtaskDefined(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), child));
        managers.getTaskManager().update(parent);
        managers.getJournalEntryManager().record(parent, entry);
    }

    private void recordConflictInParent(Task<Id, Definition, Type> parent, Task conflictingTask){
        var entry = parent.record(new BodyChanged(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), conflictingTask));
        managers.getJournalEntryManager().record(parent, entry);
    }

    private void disownExpectedUpTheTrace(){
        for (var entry: trace){
            disown(entry.getExecutedTask(), entry.getExpectedSubtaskIds());
        }
    }

    private void disown(Task<Id, Definition, Type> task, Deque<Id> toDisown){
        if (toDisown.isEmpty()){
            log.atFine().log("No subtasks to disown for task %s", task.getId());
        } else {
            var pivot = toDisown.peekFirst();
            var currentSubtasks = task.getSubtasks();
            var keptSubtasks = currentSubtasks.stream().takeWhile(x -> !x.getId().equals(pivot)).toList();
            var disownedSubtasks = currentSubtasks.subList(keptSubtasks.size(), currentSubtasks.size());
            require(
                disownedSubtasks.stream().map(Task::getId).toList().equals(toDisown.stream().toList()),
                "All the disowned tasks must be at the end of the subtask list of the parent"
            );
            log.atFine().log("Disowning %s subtasks of task %s: %s", disownedSubtasks.size(), task.getId(), toDisown);
            disownedSubtasks.clear();
            toDisown.clear();
            for (var disowned: disownedSubtasks){
                var disownedEntry = task.record(new SubtaskDisowned(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), disowned));
                var orphanedEntry = disowned.record(new DisownedByParent(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
                managers.getJournalEntryManager().record(task, disownedEntry);
                managers.getJournalEntryManager().record(disowned, orphanedEntry);
            }
            managers.getTaskManager().update(task);
        }
    }
}
