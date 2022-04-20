package com.github.filipmalczak.storyteller.impl.stack;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.api.stack.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.InstructionsSkipped;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.TaskEnded;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.TaskStarted;
import com.google.common.flogger.FluentLogger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.valid4j.Assertive;

import java.util.*;

import static java.util.Collections.reverse;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PROTECTED)
@RequiredArgsConstructor
abstract class AbstractTaskExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, Body> {
    final NitriteStackedExecutor.NitriteStackedExecutorInternals<Id, Definition, Type> internals;
    final Definition definition;
    final Type type;
    final Body body;
    final SubtaskOrderingStrategy<Id> orderingStrategy;

//    Expectations expectations;
    Optional<Task<Id, Definition, Type>> parent;
    IdGenerator<Id, Definition, Type> idGenerator;

    Id id;
    @Getter
    Task<Id, Definition, Type> thisTask;

    boolean defined;
    boolean finished;
    
    protected abstract FluentLogger getLogger();

//    protected abstract Expectations getExpected();

//    private Optional<Deque<Id>> getExpected(){
//        return internals.trace().stream().findFirst().map(TraceEntry::getExpectedSubtaskIds);
//    }

    private Optional<Task<Id, Definition, Type>> getParent(){
        return internals.trace().stream().findFirst().map(TraceEntry::getExecutedTask);
    }

    private void init() {
        getLogger().atFine().log("Executing '%s' of type %s", definition, type);
//        expectations = getExpected();
        parent = getParent();
//        getLogger().atFine().log("Known subtasks of parent: %s", expectations); //todo
        idGenerator = internals.idGeneratorFactory().over(definition, type);
    }

    protected void validateSubtaskContract() {
        require(!internals.trace().isEmpty(), "Non-root task needs to be executed with at least one task at the stack");
        require(parent.isPresent(), "Non-root task must have a parent");
    }

    protected abstract void validateContract();

    private void build() {
        defined = false;
        if (!orderingStrategy.hasExpectations()) {
            id = idGenerator.generate();
            //todo different log for root
            getLogger().atFine().log("Task wasn't defined yet; generated ID: %s", id);
            if (parent.isPresent() && isParentFinished()) {
                getLogger().atFine().log("Parent was finished; extending parent");
                recordInParent(internals.journalEntryFactory().nodeExtended());
                disownExpectedUpTheTrace();
            }
        } else {
            var candidates= orderingStrategy.getCandidatesForReusing();
            getLogger().atFine().log("Candidate IDs for reusing: %s", candidates);
            var reusable = candidates.stream().filter(idGenerator::canReuse).toList();
            getLogger().atFine().log("Reusable IDs: %s", reusable);
            require(reusable.size() < 2, "At most 1 ID can be reusable");
            if (reusable.size() == 1) {
                defined = true;
                id = orderingStrategy.reuse(reusable.get(0));
                getLogger().atFine().log("Reusing ID of already defined task: %s", id);
            } else {
                orderingStrategy.onNoReusable(candidates);
            }
        }
        var found = internals.managers().getTaskManager().findById(id);
        getLogger().atFine().log("Retrieved task: %s", found.map(t -> t.getId()+"::"+t.getDefinition()));
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
            internals.managers().getTaskManager().register(thisTask);
        }
    }

    protected abstract void handleRunning();

    private void lifecycle() {
        if (parent.isPresent())
            if (!defined) {
                defineInParent();
            } else {
                getLogger().atFine().log("Subtask %s already defined", id);
            }
        finished = isFinished();
        if (!isStarted()) {
            record(internals.journalEntryFactory().taskStarted());
            getLogger().atFine().log("Recorded the start of task %s", id);
        } else {
            getLogger().atFine().log("Task %s already started", id);
        }
        internals.history().start(id, parent.map(Task::getId));
        handleBody();
        internals.history().add(id, id, type.isLeaf());
        for (var traceEntry : internals.trace()) {
            internals.history().add(traceEntry.getExecutedTask().getId(), id, type.isLeaf());
        }
        internals.trace().stream().findFirst().ifPresent(e -> e.getStorage().reload());
        if (!finished) {
            record(internals.journalEntryFactory().taskEnded());
            getLogger().atFine().log("Recorded the end of task %s", id);
        } else {
            getLogger().atFine().log("Task %s already finished", id);
        }
    }

    @SneakyThrows
    private void handleBody() {
        try {
            handleRunning();
        } catch (Exception e) {
            //todo test proper rethrowing of exceptions
            if (e instanceof AlreadyRecordedException) {
                if (type.isRoot()) {
                    throw ((AlreadyRecordedException) e).getAlreadyRecorded();
                } else {
                    throw e;
                }
            } else {
                record(internals.journalEntryFactory().exceptionCaught(e));
                if (type.isRoot()) {
                    throw e;
                } else {
                    throw new AlreadyRecordedException(e);
                }
            }
        }
    }

    public void run() {
        init();
        validateContract();
        build();
        lifecycle();
    }

    private boolean isStarted() {
        var firstJournalEntry = thisTask.getJournalEntries().findFirst();
        if (firstJournalEntry.isEmpty())
            return false;
        require(firstJournalEntry.get() instanceof TaskStarted, "First journal entry must describe starting the task");
        return true;
    }

    private boolean isFinished() {
        return isTaskFinished(thisTask);
    }

    private boolean isParentFinished() {
        return isTaskFinished(parent.get());
    }

    private static boolean isTaskFinished(Task task) {
        var entries = new ArrayList<>(task.getJournalEntries().toList());
        if (entries.isEmpty())
            return false;
        reverse(entries);
        var firstNonSkip = entries.stream().dropWhile(e -> e instanceof InstructionsSkipped).findFirst();
        require(firstNonSkip.isPresent(), "Journal has to consist of something else than just 'skip' records");
        return firstNonSkip.get() instanceof TaskEnded;
    }

    private void recordEntry(Task<Id, Definition, Type> task, JournalEntry entry){
        getLogger().atFine().log("Recording for %s: %s", task.getId(), entry);
        internals.managers()
            .getJournalEntryManager()
            .record(
                task,
                task.record(entry)
            );
    }

    protected void record(JournalEntry entry) {
        recordEntry(thisTask, entry);
    }

    protected void recordInParent(JournalEntry entry) {
        recordEntry(parent.get(), entry);
    }

    private void defineInParent() {
        var p = parent.get();
        getLogger().atFine().log("Defining subtask %s in parent %s", id, p.getId());
        p.getSubtasks().add(thisTask);
        internals.managers().getTaskManager().update(p);
        recordInParent(internals.journalEntryFactory().subtaskDefined(thisTask));
    }

    protected void disownExpectedUpTheTrace() {
        for (var entry : internals.trace()) {
            disownExpectationsThatAreLeft(entry);
            //sublist(1), because
//            var toDisown = new LinkedList<>(entry.getExpectedSubtaskIds());
////            toDisown.remove(0);
//            disown(entry.getExecutedTask(), toDisown);
        }
    }

    private void disownExpectationsThatAreLeft(TraceEntry<Id, Definition, Type> entry) {
        disownSubtasks(entry.getExecutedTask(), entry.getExpectedSubtaskIds());
        entry.getExpectedSubtaskIds().clear();
    }

    private void disownSubtasks(Task<Id, Definition, Type> task, List<Id> toDisown) {
        if (toDisown.isEmpty()) {
            getLogger().atFine().log("No subtasks to disown for task %s", task.getId());
        } else {
            var pivot = toDisown.get(0);
            var currentSubtasks = task.getSubtasks();
            var keptSubtasks = currentSubtasks.stream().takeWhile(x -> !x.getId().equals(pivot)).toList();
            var disownedSubtasks = currentSubtasks.subList(keptSubtasks.size(), currentSubtasks.size());
            require(
                disownedSubtasks.stream().map(Task::getId).toList().equals(toDisown.stream().toList()),
                "All the disowned tasks must be at the end of the subtask list of the parent"
            );
            getLogger().atFine().log("Disowning %s subtasks of task %s: %s", disownedSubtasks.size(), task.getId(), toDisown);
            for (var disowned : disownedSubtasks) {
                recordEntry(task, internals.journalEntryFactory().subtaskDisowned(disowned));
                recordEntry(disowned, internals.journalEntryFactory().disownedByParent());
            }
            disownedSubtasks.clear();
            toDisown.clear();

            //fioxme
            internals.managers().getTaskManager().update(task);
        }
    }

    public ExecutionFriend<Id, Definition, Type> getFriend(){
        return new ExecutionFriend<Id, Definition, Type>() {
            @Override
            public void recordInParent(JournalEntry entry) {
                AbstractTaskExecution.this.recordInParent(entry);
            }

            @Override
            public void disownExpectedUpTheTrace() {
                AbstractTaskExecution.this.disownExpectedUpTheTrace();
            }

            @Override
            public void setId(Id id) {
                AbstractTaskExecution.this.id = id;
            }

            @Override
            public JournalEntryFactory journalEntryFactory() {
                return AbstractTaskExecution.this.internals.journalEntryFactory();
            }

            @Override
            public IdGenerator<Id, Definition, Type> idGenerator() {
                return AbstractTaskExecution.this.idGenerator;
            }

            @Override
            public Optional<Task<Id, Definition, Type>> findTask(Id id) {
                return AbstractTaskExecution.this.internals.managers().getTaskManager().findById(id);
            }

            @Override
            public Id parentId() {
                return AbstractTaskExecution.this.parent.get().getId();
            }
        };
    }
}
