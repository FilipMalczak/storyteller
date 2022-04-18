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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

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

    Deque<Id> expectations;
    Optional<Task<Id, Definition, Type>> parent;
    IdGenerator<Id, Definition, Type> idGenerator;

    Id id;
    @Getter
    Task<Id, Definition, Type> thisTask;

    boolean defined;
    boolean finished;
    
    protected abstract FluentLogger getLogger();


    private Optional<Deque<Id>> getExpected(){
        return internals.trace().stream().findFirst().map(TraceEntry::getExpectedSubtaskIds);
    }

    private Optional<Task<Id, Definition, Type>> getParent(){
        return internals.trace().stream().findFirst().map(TraceEntry::getExecutedTask);
    }

    private void init() {
        getLogger().atFine().log("Executing '%s' of type %s", definition, type);
        expectations = getExpected().orElseGet(LinkedList::new);
        parent = getParent();
        getLogger().atFine().log("Known subtasks of parent: %s", expectations);
        idGenerator = internals.idGeneratorFactory().over(definition, type);
    }

    protected void validateSubtaskContract() {
        require(!internals.trace().isEmpty(), "Non-root task needs to be executed with at least one task at the stack");
        require(parent.isPresent(), "Non-root task must have a parent");
        require(!type.isChoice(), "Choice tasks should be executed with chooseNextSteps(...) method");
    }

    protected abstract void validateContract();

    private void build() {
        defined = false;
        if (expectations.isEmpty()) {
            id = idGenerator.generate();
            getLogger().atFine().log("Task wasn't defined yet; generated ID: %s", id);
            if (parent.isPresent() && isParentFinished()) {
                getLogger().atFine().log("Parent was finished; extending parent");
                recordInParent(internals.journalEntryFactory().nodeExtended());
                disownExpectedUpTheTrace();
            }
        } else {
            //we pop it here
            id = expectations.removeFirst();
            if (idGenerator.canReuse(id)) {
                defined = true;
                getLogger().atFine().log("Reusing ID of already defined task: %s", id);
            } else {
                //so in case of conflict
                var conflicting = internals.managers().getTaskManager().findById(id);
                Assertive.require(conflicting.isPresent(), "Non-reusable ID must refer to an existing task");
                var conflictingTask = conflicting.get();
                getLogger().atFine().log("Conflicting subtask of task %s: definition was %s, but is now %s", parent.get().getId(), conflictingTask.getDefinition(), definition);
                Assertive.require(conflictingTask.getDefinition(), not(equalTo(definition)));
                recordInParent(internals.journalEntryFactory().bodyChanged(conflictingTask));
                //we need to push it back to top
                expectations.addFirst(id);
                //so it can be disowned in correct order
                disownExpectedUpTheTrace();
                id = idGenerator.generate();
                getLogger().atFine().log("Recovered from conflict; generated new ID: %s", id);
            }
        }
        var found = internals.managers().getTaskManager().findById(id);
        getLogger().atFine().log("Retrieved task: %s", found);
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
                throw new AlreadyRecordedException(e);
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

    private void recordInParent(JournalEntry entry) {
        recordEntry(parent.get(), entry);
    }

    private void defineInParent() {
        var p = parent.get();
        getLogger().atFine().log("Defining subtask %s in parent %s", id, p.getId());
        p.getSubtasks().add(thisTask);
        internals.managers().getTaskManager().update(p);
        recordInParent(internals.journalEntryFactory().subtaskDefined(thisTask));
    }

    private void disownExpectedUpTheTrace() {
        for (var entry : internals.trace()) {
            disown(entry.getExecutedTask(), entry.getExpectedSubtaskIds());
        }
    }

    private void disown(Task<Id, Definition, Type> task, Deque<Id> toDisown) {
        if (toDisown.isEmpty()) {
            getLogger().atFine().log("No subtasks to disown for task %s", task.getId());
        } else {
            var pivot = toDisown.peekFirst();
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
            internals.managers().getTaskManager().update(task);
        }
    }
}
