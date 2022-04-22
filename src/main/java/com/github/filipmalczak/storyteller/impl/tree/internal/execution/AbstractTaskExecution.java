package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.id.IdGenerator;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.InstructionsSkipped;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskEnded;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskStarted;
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

import java.util.*;

import static java.util.Collections.reverse;
import static org.hamcrest.CoreMatchers.not;
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

    private void build() {
        defined = false;
        amended = false;
        if (!orderingStrategy.hasExpectations()) {
            id = idGenerator.generate();
            //todo different log for root
            getLogger().atFine().log("Task wasn't defined yet; generated ID: %s", id);
            if (parent.isPresent() && isParentFinished()) {
                getLogger().atFine().log("Parent was finished; extending parent");
                internals.events().bodyExtended(getParent().get());
                amended = true;
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
                amended = true;
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
        }
        internals.history().start(id, parent.map(Task::getId));
        handleBody();
        //fixme this is not the place to notice shrinking
//        if (orderingStrategy.hasExpectations()) {
//            //todo this is pretty messy, make it nicer
//            var leftovers = orderingStrategy.getCandidatesForReusing()
//                .stream()
//                .map(internals.managers().getTaskManager()::getById)
//                .toList();
//            internals.events().bodyShrunk(
//                thisTask,
//                leftovers.stream().map(t -> (Task) t).toList()
//            );
//            getLogger().atFine().log("Task %s has shrunk", id);
//            disownSubtasks(thisTask, orderingStrategy.getCandidatesForReusing().stream().toList());
//            disownExpectedUpTheTrace();
//        }
        internals.history().add(id, id, type.isLeaf());
        for (var traceEntry : internals.trace()) {
            internals.history().add(traceEntry.getExecutedTask().getId(), id, type.isLeaf());
        }
        internals.trace().stream().findFirst().ifPresent(e -> e.getStorage().reload());
        if (!finished) {
            internals.events().taskEnded(thisTask);
            getLogger().atFine().log("Ended task %s", id);
        } else {
            getLogger().atFine().log("Task %s already finished", id);
        }
        if (recordIncorporateToParent && parent.isPresent()) {
            internals.events().subtaskIncorporated(parent.get(), thisTask);
        }

    }

    @SneakyThrows
    private void handleBody() {
        try {
            handleRunning();
        } catch (Exception e) {
            //todo test proper rethrowing of exceptions
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
        var entries = new ArrayList<>(task.getJournalEntries().toList());
        if (entries.isEmpty())
            return false;
        reverse(entries);
        var firstNonSkip = entries.stream().dropWhile(e -> e instanceof InstructionsSkipped).findFirst();
        require(firstNonSkip.isPresent(), "Journal has to consist of something else than just 'skip' records");
        return firstNonSkip.get() instanceof TaskEnded;
    }

    private void defineInParent() {
        var p = parent.get();
        getLogger().atFine().log("Defining subtask %s in parent %s", id, p.getId());
        p.getSubtasks().add(thisTask);
        internals.managers().getTaskManager().update(p);
        internals.events().defineSubtask(p, thisTask);
    }

    protected void disownExpectedUpTheTrace() {
        for (var entry : internals.trace()) {
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
            var currentSubtasks = task.getSubtasks();
            var keptSubtasks = currentSubtasks.stream().takeWhile(x -> !x.getId().equals(pivot)).toList();
            var disownedSubtasks = currentSubtasks.subList(keptSubtasks.size(), currentSubtasks.size());
            require(
                disownedSubtasks.stream().map(Task::getId).toList().equals(toDisown.stream().toList()),
                "All the disowned tasks must be at the end of the subtask list of the parent"
            );
            getLogger().atFine().log("Disowning %s subtasks of task %s: %s", disownedSubtasks.size(), task.getId(), toDisown);
            internals.events().subtasksDisowned(task, disownedSubtasks);
            disownedSubtasks.clear();
            toDisown.clear(); //this is passed by reference and actually points to the same list as the trace entry
            internals.managers().getTaskManager().update(task);
        }
    }

    public ExecutionFriend<Id, Definition, Type> getFriend(){
        return new ExecutionFriend<>() {

            @Override
            public void disownExpectedUpTheTrace() {
                AbstractTaskExecution.this.disownExpectedUpTheTrace();
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
