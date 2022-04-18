package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.StackedExecutor;
import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.stack.task.body.TaskBody;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGeneratorFactory;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.stack.data.NitriteManagers;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadStorage;
import com.github.filipmalczak.storyteller.impl.storage.NitriteReadWriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Collections.reverse;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.valid4j.Assertive.neverGetHere;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@EqualsAndHashCode
@Flogger
public class NitriteStackedExecutor<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements StackedExecutor<Id, Definition, Type, Nitrite> {
    @NonNull NitriteManagers<Id, Definition, Type> managers;
    @NonNull HistoryTracker<Id> history;
    @NonNull NitriteStorageConfig<Id> storageConfig;
    @NonNull IdGeneratorFactory<Id, Definition, Type> idIdGeneratorFactory;
    @NonNull List<TraceEntry<Id, Definition, Type>> trace; // trace[0] - parent; trace[-1] - root; empty for root
    JournalEntryFactory journalEntryFactory;


    public NitriteStackedExecutor(@NonNull NitriteManagers<Id, Definition, Type> managers, @NonNull HistoryTracker<Id> history, @NonNull NitriteStorageConfig<Id> storageConfig, @NonNull IdGeneratorFactory<Id, Definition, Type> idIdGeneratorFactory, @NonNull List<TraceEntry<Id, Definition, Type>> trace) {
        this.managers = managers;
        this.history = history;
        this.storageConfig = storageConfig;
        this.idIdGeneratorFactory = idIdGeneratorFactory;
        this.trace = trace;
        this.journalEntryFactory = new JournalEntryFactory(managers.getSessionManager());
    }

    private Optional<Deque<Id>> getExpected(){
        return trace.stream().findFirst().map(TraceEntry::getExpectedSubtaskIds);
    }

    private Optional<Task<Id, Definition, Type>> getParent(){
        return trace.stream().findFirst().map(TraceEntry::getExecutedTask);
    }

    private void recordEntry(Task<Id, Definition, Type> task, JournalEntry entry){
        managers
            .getJournalEntryManager()
            .record(
                task,
                task.record(entry)
            );
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    private class TaskExecution {
        Definition definition;
        Type type;
        TaskBody<Id, Definition, Type, Nitrite> body;

        Deque<Id> expectations;
        Optional<Task<Id, Definition, Type>> parent;
        IdGenerator<Id, Definition, Type> idGenerator;

        Id id;
        @Getter Task<Id, Definition, Type> thisTask;

        boolean leaf;
        boolean defined;
        boolean skip;
        boolean finished;

        public TaskExecution(Definition definition, Type type, TaskBody<Id, Definition, Type, Nitrite> body) {
            this.definition = definition;
            this.type = type;
            this.body = body;
        }

        private void init(){
            log.atFine().log("Executing '%s' of type %s", definition, type);
            expectations = getExpected().orElseGet(LinkedList::new);
            parent = getParent();
            log.atFine().log("Known subtasks of parent: %s", expectations);
            idGenerator = idIdGeneratorFactory.over(definition, type);
        }

        private void validateContract(){
            if (type.isRoot()) {
                require(trace.isEmpty(), "Root task needs to be executed without any tasks at the stack");
                require(parent.isEmpty(), "Root task cannot have a parent");
                require(expectations.isEmpty(), "Root task cannot have expected ID");
                require(body instanceof NodeBody, "Root task body must be implemented as %1", NodeBody.class.getCanonicalName());
            } else {
                require(!trace.isEmpty(), "Non-root task needs to be executed with at least one task at the stack");
                require(parent.isPresent(), "Non-root task must have a parent");
                require(!type.isChoice(), "Choice tasks should be executed with chooseNextSteps(...) method");
                if (type.isLeaf()) {
                    require(body instanceof LeafBody, "Leaf task body must be implemented as %1", LeafBody.class.getCanonicalName());
                } else {
                    require(body instanceof NodeBody, "Node task body must be implemented as %1", NodeBody.class.getCanonicalName());
                }
            }
        }

        private void build(){
            defined = false;
            if (expectations.isEmpty()) {
                id = idGenerator.generate();
                log.atFine().log("Task wasn't defined yet; generated ID: %s", id);
                if (parent.isPresent() && isParentFinished()){
                    log.atFine().log("Parent was finished; extending parent");
                    recordInParent(journalEntryFactory.nodeExtended());
                    disownExpectedUpTheTrace();
                }
            } else {
                //we pop it here
                id = expectations.removeFirst();
                if (idGenerator.canReuse(id)){
                    defined = true;
                    log.atFine().log("Reusing ID of already defined task: %s", id);
                } else {
                    //so in case of conflict
                    var conflicting = managers.getTaskManager().findById(id);
                    require(conflicting.isPresent(), "Non-reusable ID must refer to an existing task");
                    var conflictingTask = conflicting.get();
                    log.atFine().log("Conflicting subtask of task %s: definition was %s, but is now %s", parent.get().getId(), conflictingTask.getDefinition(), definition);
                    require(conflictingTask.getDefinition(), not(equalTo(definition)));
                    recordInParent(journalEntryFactory.bodyChanged(conflictingTask));
                    //we need to push it back to top
                    expectations.addFirst(id);
                    //so it can be disowned in correct order
                    disownExpectedUpTheTrace();
                    id = idGenerator.generate();
                    log.atFine().log("Recovered from conflict; generated new ID: %s", id);
                }
            }
            var found = managers.getTaskManager().findById(id);
            log.atFine().log("Retrieved task: %s", found);
            thisTask = found
                .orElseGet(
                    () -> Task.<Id, Definition, Type>builder()
                        .id(id)
                        .definition(definition)
                        .type(type)
                        .build()
                );
            if (found.isPresent()) {
                require(thisTask.getDefinition(), equalTo(definition));
                require(thisTask.getType(), equalTo(type));
            } else {
                managers.getTaskManager().register(thisTask);
            }
        }

        private void lifecycle(){
            if (parent.isPresent() && !defined){
                defineInParent();
            }
            leaf = type.isLeaf();
            finished = isFinished();
            skip = leaf && finished;
            if (!isStarted()){
                record(journalEntryFactory.taskStarted());
            }
            history.start(id, parent.map(Task::getId));
            if (!skip) {
                runBody();
                finished = isFinished();
            } else {
                record(journalEntryFactory.instructionsSkipped());
            }
            for (var traceEntry: trace){
                history.add(traceEntry.getExecutedTask().getId(), id, leaf);
            }
            trace.stream().findFirst().ifPresent(e -> e.getStorage().reload());
            if (!finished) {
                record(journalEntryFactory.taskEnded());
            }
        }

        public void run(){
            init();
            validateContract();
            build();
            lifecycle();
        }

        private boolean isStarted(){
            var firstJournalEntry = thisTask.getJournalEntries().findFirst();
            if (firstJournalEntry.isEmpty())
                return false;
            require(firstJournalEntry.get() instanceof TaskStarted, "First journal entry must describe starting the task");
            return true;
        }

        private boolean isFinished(){
            return isTaskFinished(thisTask);
        }

        private boolean isParentFinished(){
            return isTaskFinished(parent.get());
        }

        private static boolean isTaskFinished(Task task){
            var entries = new ArrayList<>(task.getJournalEntries().toList());
            if (entries.isEmpty())
                return false;
            reverse(entries);
            var firstNonSkip = entries.stream().dropWhile(e -> e instanceof InstructionsSkipped).findFirst();
            require(firstNonSkip.isPresent(), "Journal has to consist of something else than just 'skip' records");
            return firstNonSkip.get() instanceof TaskEnded;
        }


        @SneakyThrows
        private void runBody(){
            //todo missing define and integrate; are they still needed though?
            record(journalEntryFactory.instructionsRan());
            try {
                if (body instanceof NodeBody)
                    runNode(thisTask, (NodeBody<Id, Definition, Type, Nitrite>) body);
                else if (body instanceof LeafBody)
                    runLeaf(thisTask, (LeafBody<Id, Definition, Type, Nitrite>) body);
                else
                    neverGetHere(); //todo this should be coverable with type system
            } catch (Exception e) {
                //todo test proper rethrowing of exceptions
                if (e instanceof AlreadyRecordedException){
                    if (thisTask.getType().isRoot()){
                        throw ((AlreadyRecordedException) e).alreadyRecorded;
                    }
                } else {
                    record(journalEntryFactory.exceptionCaught(e));
                    throw new AlreadyRecordedException(e);
                }
            }
        }

        private void runNode(Task<Id, Definition, Type> task, NodeBody<Id, Definition, Type, Nitrite> body){
            var storage = new NitriteReadStorage(storageConfig, history, task.getId());
            var newTrace = new LinkedList<>(trace);
            var newEntry = new TraceEntry<>(task, new LinkedList<>(task.getSubtasks().stream().map(Task::getId).toList()), storage);
            newTrace.addFirst(newEntry);
            body.perform(
                new NitriteStackedExecutor<>(
                    managers,
                    history,
                    storageConfig,
                    idIdGeneratorFactory,
                    newTrace
                ),
                storage
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

        private void record(JournalEntry entry){
            recordEntry(thisTask, entry);
        }

        private void recordInParent(JournalEntry entry){
            recordEntry(parent.get(), entry);
        }

        private void defineInParent(){
            var p = parent.get();
            p.getSubtasks().add(thisTask);
            managers.getTaskManager().update(p);
            recordInParent(journalEntryFactory.subtaskDefined(thisTask));
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
                for (var disowned: disownedSubtasks){
                    recordEntry(task, journalEntryFactory.subtaskDisowned(disowned));
                    recordEntry(disowned, journalEntryFactory.disownedByParent());
                }
                disownedSubtasks.clear();
                toDisown.clear();
                managers.getTaskManager().update(task);
            }
        }
    }

    @Override
    public Task<Id, Definition, Type> executeTask(Definition definition, Type type, TaskBody<Id, Definition, Type, Nitrite> body) {
        var execution = new TaskExecution(definition, type, body);
        execution.run();
        return execution.getThisTask();
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

}
