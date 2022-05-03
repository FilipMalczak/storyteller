package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.*;
import com.github.filipmalczak.storyteller.impl.tree.internal.ExecutionFriend;
import com.github.filipmalczak.storyteller.impl.tree.internal.NitriteTreeInternals;
import com.github.filipmalczak.storyteller.impl.tree.internal.ThrowingAlreadyRecordedException;
import com.github.filipmalczak.storyteller.impl.tree.internal.TraceEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.journal.Events;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.SubtaskOrderingStrategy;
import com.google.common.flogger.FluentLogger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PROTECTED)
@RequiredArgsConstructor
abstract class AbstractTaskExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType, Body> {
    final NitriteTreeInternals<Id, Definition, Type> internals;
    final Definition definition;
    final Type type;
    final Body body;
    final SubtaskOrderingStrategy<Id> orderingStrategy;
    final boolean recordIncorporateToParent;

//    Expectations expectations;
    Optional<Task<Id, Definition, Type>> parent;
    IdGenerator<Id, Definition, Type> idGenerator;

    Id id;
    @Getter
    Task<Id, Definition, Type> thisTask;

    boolean amended;
    boolean defined;
    boolean finished;
    
    protected abstract FluentLogger getLogger();

    private Optional<Task<Id, Definition, Type>> getParent(){
        return internals.trace().stream().findFirst().map(TraceEntry::getExecutedTask);
    }

    /**
     * nullable
     * todo cache during init()
     */
    private Id getPreviousSiblingId(){
        return internals.trace().stream().findFirst().map(TraceEntry::getLastSubtaskId).orElse(null);
    }

    private void init() {
        getLogger().atFine().log("Executing '%s' of type %s", definition, type);
        parent = getParent();
        idGenerator = internals.idGeneratorFactory().over(definition, type);
    }

    protected void validateSubtaskContract() {
        require(!internals.trace().isEmpty(), "Non-root task needs to be executed with at least one task at the stack");
        require(parent.isPresent(), "Non-root task must have a parent");
    }

    protected abstract void validateContract();

    private static <T> boolean nullableEquals(T t1, T t2){
        if (t1 == null)
            return t2 == null;
        return t1.equals(t2);
    }

    private void build() {
        defined = false;
        amended = false;
        if (!orderingStrategy.hasExpectations()) {
            id = idGenerator.generate();
            if (type.isRoot()) {
                getLogger().atFine().log("Generated reusable ID for a root node: %s", id);
            } else {
                getLogger().atFine().log("Task wasn't defined yet; generated ID: %s", id);
            }
            if (parent.isPresent() && isParentFinished()) {
                getLogger().atFine().log("Parent was finished; extending parent");
                internals.events().bodyExtended(getParent().get());
                amended = true;
                disownExpectedUpTheTrace();
            }
        } else {
            var candidateIds= orderingStrategy.getCandidatesForReusing();
            getLogger().atFine().log("Candidate IDs for reusing: %s", candidateIds);
            var reusableIds = candidateIds.stream().filter(idGenerator::canReuse).toList();
            getLogger().atFine().log("Reusable IDs: %s", reusableIds);
            var reusable = reusableIds
                .stream()
                .map(internals.managers().getTaskManager()::getById)
                .filter(
                    t ->
                        nullableEquals(t.getPreviousSiblingId(), getPreviousSiblingId()) &&
                        nullableEquals(t.getParentId(), parent.map(Task::getId).orElse(null))
                )
                .toList();
            //todo
            require(reusable.size() < 2, "At most 1 ID can be reusable");
            if (reusable.size() == 1) {
                defined = true;
                thisTask = reusable.get(0);
                id = orderingStrategy.reuse(thisTask.getId());
                getLogger().atFine().log("Reusing ID of already defined task: %s", id);
            } else {
                orderingStrategy.onNoReusable(candidateIds);
                amended = true; //this will be modified later, and will stay true only if the task has been finished
                getLogger().atFine().log("No IDs are reusable and referring to a task matching a parent and previous sibling");
            }
        }
        var found = internals.managers().getTaskManager().findById(id);
        getLogger().atFine().log("Retrieved task: %s", found.map(t -> t.getId()+"::"+t.getDefinition()));
        thisTask = found
            .orElseGet(
                () -> SimpleTask.<Id, Definition, Type>builder()
                    .id(id)
                    .definition(definition)
                    .parentId(parent.map(Task::getId).orElse(null))
                    .previousSiblingId(getPreviousSiblingId())
                    .type(type)
                    .taskResolver(internals.managers().getTaskManager())
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
        amended &= finished;
        if (!isStarted()) {
            internals.events().taskStarted(thisTask);
            getLogger().atFine().log("Started task %s", id);
        } else {
            getLogger().atFine().log("Task %s already started", id);
        }
        if (amended) {
            var conflicting = orderingStrategy.getConflicts();
            if (conflicting != null) {
                var conflicts = conflicting.stream()
                    .map(
                        internals
                            .managers()
                            .getTaskManager()::getById
                    )
                    .toList();
                internals.events().bodyChanged(
                    thisTask,
                    conflicts
                );
                //todo this could be optimized
                disownSubtasks(thisTask, conflicting);
                disownExpectedUpTheTrace();
            }
            internals.events().taskAmended(thisTask);
            getLogger().atFine().log("Amended task %s", id);
            finished = false;
        }
        internals.history().start(id, parent.map(Task::getId));
        handleBody();
        internals.history().add(id, id, type.isWriting());
        for (var traceEntry : internals.trace()) {
            internals.history().add(traceEntry.getExecutedTask().getId(), id, type.isWriting());
        }
        internals.trace().stream().findFirst().ifPresent(e -> e.getStorage().reload());
        if (!finished) {
            internals.events().taskEnded(thisTask);
            getLogger().atFine().log("Ended task %s", id);
        } else {
            getLogger().atFine().log("Task %s already finished", id);
        }
        if (recordIncorporateToParent && parent.isPresent()) {
            internals.events().subtaskIncorporated(parent.get(), id);
        }
        if (!internals.trace().isEmpty()) {
            var traceEntry = internals.trace().get(0);
            traceEntry.setLastSubtaskId(thisTask.getId());
            getLogger().atFine().log("Last subtask ID of %s set to %s", thisTask.getParentId(), thisTask.getId());
        }
    }

    @SneakyThrows
    private void handleBody() {
        try {
            handleRunning();
        } catch (Exception e) {
            //todo log it nicely; flogger has withCause() method
            if (e instanceof ThrowingAlreadyRecordedException) {
                internals.events().taskInterrupted(thisTask);
                if (type.isRoot()) {
                    throw ((ThrowingAlreadyRecordedException) e).getAlreadyRecorded();
                } else {
                    throw e;
                }
            } else {
                internals.events().exeptionCaught(thisTask, e);
                if (type.isRoot()) {
                    throw e;
                } else {
                    throw new ThrowingAlreadyRecordedException(e);
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
        var entries = new ArrayList<JournalEntry>(task.getJournalEntries().toList());
        if (entries.isEmpty())
            return false;
        reverse(entries);
        var endAugmentOrAmend = entries.stream()
            .filter(e ->
                e instanceof TaskEnded ||
                e instanceof TaskAmended ||
                e instanceof ParallelNodeAugmented
            )
            .findFirst();
        return endAugmentOrAmend.map(e -> e instanceof TaskEnded).orElse(false);
    }

    private void defineInParent() {
        var p = parent.get();
        getLogger().atFine().log("Defining subtask %s in parent %s", id, p.getId());
        internals.events().defineSubtask(p, thisTask);
    }

    protected void disownExpectedUpTheTrace() {
        disownExpectedUpTheTrace(internals.trace());
    }

    protected void disownExpectedUpTheTrace(List<TraceEntry<Id, Definition, Type>> trace) {
        for (var entry : trace) {
            disownExpectationsThatAreLeft(entry);
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
            var currentSubtasks = task.getSubtaskIds().toList();
            var keptSubtasks = currentSubtasks.stream().takeWhile(not(isEqual(pivot))).toList();
            var disownedSubtasks = currentSubtasks.subList(keptSubtasks.size(), currentSubtasks.size());
            require(
                disownedSubtasks.equals(toDisown),
                "All the disowned tasks must be at the end of the subtask list of the parent"
            );
            getLogger().atFine().log("Disowning %s subtasks of task %s: %s", disownedSubtasks.size(), task.getId(), toDisown);
            internals.events().subtasksDisowned(task, disownedSubtasks);
        }
    }

    public ExecutionFriend<Id, Definition, Type> getFriend(){
        return new ExecutionFriend<>() {

            @Override
            public void disownExpectedUpTheTrace() {
                AbstractTaskExecution.this.disownExpectedUpTheTrace();
            }

            @Override
            public void disownSubtask(Task<Id, Definition, Type> task, Id toDisown) {
                AbstractTaskExecution.this.disownSubtasks(task, asList(toDisown));
            }

            @Override
            public void setId(Id id) {
                AbstractTaskExecution.this.id = id;
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

            @Override
            public Events<Id> events() {
                return AbstractTaskExecution.this.internals.events();
            }
        };
    }
}
