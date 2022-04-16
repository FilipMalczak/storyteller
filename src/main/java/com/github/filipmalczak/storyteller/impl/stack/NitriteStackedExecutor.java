package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadWriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.stack.task.*;
import com.github.filipmalczak.storyteller.stack.task.journal.entries.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.reverse;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.valid4j.Assertive.neverGetHere;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Flogger
public class NitriteStackedExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements StackedExecutor<Id, Definition, Type> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idIdGeneratorFactory;
    @NonNull List<Id> trace; //fixme this is only used for contract checks and debugging; remove it?
    @NonNull Optional<Task<Id, Definition, Type>> parent;
    @NonNull Queue<Id> expected;

    @Override
    public Task<Id, Definition, Type> executeTask(Definition definition, Type type, TaskBody<Id, Definition, Type> body) {
        log.atFine().log("Executing '%s' of type %s", definition, type);
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
        if (expected.isEmpty()) {
            id = generator.generate();
            log.atFine().log("Task wasn't defined yet; generated ID: %s", id);
            if (parent.isPresent() && isFinished(parent.get())){
                log.atFine().log("Parent was finished; extending parent");
                extend(parent.get());
            }
        } else {
            id = expected.remove();
            require(generator.canReuse(id), "Expected task ID must be reusable");
            isDefined = true;
            log.atFine().log("Reusing ID of already defined task: %s", id);
        }
        var found = managers.getTaskManager().findById(id);
        log.atFine().log("Retrieved task: %s", found);
        var task = found
            .orElseGet(
                () -> Task.<Id, Definition, Type>builder()
                    .id(id)
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
        boolean finished = isFinished(task);
        boolean skip = body instanceof LeafBody && finished;
        if (!isStarted(task)){
            start(task);
        }
        tracker.put(id, parent.map(Task::getId).map(tracker::get).map(Stream::toList).orElseGet(ArrayList::new));
        if (!skip) {
            runBody(task, body);
            finished = isFinished(task);
        } else {
            recordSkipping(task);
        }
        if (parent.isPresent()){
            tracker.add(parent.get().getId(), id);
        }
        if (!finished) {
            end(task);
        }
        //todo I think integrate step is useless now
        if (parent.isPresent() && !isIntegrated(parent.get(), task)){
            integrateIntoParent(parent.get(), task);
        }
        return task;
    }

    private boolean isStarted(Task task){
        var firstJournalEntry = task.getJournalEntries().findFirst();
        if (firstJournalEntry.isEmpty())
            return false;
        require(firstJournalEntry.get() instanceof StartTask, "First journal entry must describe starting the task");
        return true;
    }

    private boolean isFinished(Task task){
        var entries = new ArrayList<>(task.getJournalEntries().toList());
        if (entries.isEmpty())
            return false;
        reverse(entries);
        var firstNonSkip = entries.stream().dropWhile(e -> e instanceof SkipAlreadyExecuted).findFirst();
        require(firstNonSkip.isPresent(), "Journal has to consist of something else than just 'skip' records");
        return firstNonSkip.get() instanceof EndTask;
    }

    private void extend(Task task){
        var entry = task.record(new NodeExtended(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void start(Task task){
        var entry = task.record(new StartTask(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
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
    private void runBody(Task<Id, Definition, Type> task, TaskBody<Id, Definition, Type> body){
        //todo missing define and integrate; are they still needed though?
        recordRunning(task);
        try {
            if (body instanceof NodeBody)
                runNode(task, (NodeBody<Id, Definition, Type>) body);
            else if (body instanceof LeafBody)
                runLeaf(task, (LeafBody) body);
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

    private void runNode(Task<Id, Definition, Type> task, NodeBody<Id, Definition, Type> body){
        List<Id> newTrace = new LinkedList<>(trace);
        newTrace.add(task.getId());
        Queue<Id> newExpected = new LinkedList<>(task.getSubtasks().stream().map(Task::getId).toList());
        body.perform(
            new NitriteStackedExecutor<>(
                managers,
                tracker,
                storageConfig,
                idIdGeneratorFactory,
                newTrace,
                Optional.of(task),
                newExpected
            ),
            new NitriteReadStorage(storageConfig, tracker, task.getId())
        );
    }

    private void runLeaf(Task<Id, Definition, Type> task, LeafBody body){
        body.perform(new NitriteReadWriteStorage(storageConfig, tracker, task.getId()));
    }

    private void recordException(Task task, Exception e){
        //todo extract stack trace to string
        var entry = task.record(new CatchException(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), e.getMessage(), ""));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void recordRunning(Task task){
        var entry = task.record(new RunIntructions(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void recordSkipping(Task task){
        var entry = task.record(new SkipAlreadyExecuted(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private void end(Task task){
        var entry = task.record(new EndTask(managers.getSessionManager().getCurrent(), ZonedDateTime.now()));
        managers.getJournalEntryManager().record(task, entry);
    }

    private boolean isIntegrated(Task<?,?,?> parent, Task<Id, ?, ?> child){
        return parent.getJournalEntries()
            .filter(e -> e instanceof IntegrateSubtask)
            .map(e -> (IntegrateSubtask) e)
            .anyMatch(e -> e.getIntegrated().getId().equals(child.getId()));
    }

    private void defineInParent(Task<Id, Definition, Type> parent, Task child){
        parent.getSubtasks().add(child);
        var entry = parent.record(new DefineSubtask(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), child));
        managers.getTaskManager().update(parent);
        managers.getJournalEntryManager().record(parent, entry);
    }

    private void integrateIntoParent(Task<Id, Definition, Type> parent, Task<Id, ?, ?> child){
        var entry = parent.record(new IntegrateSubtask(managers.getSessionManager().getCurrent(), ZonedDateTime.now(), child));
        managers.getJournalEntryManager().record(parent, entry);
    }
}
