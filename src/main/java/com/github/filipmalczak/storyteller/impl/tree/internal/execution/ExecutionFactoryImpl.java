package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskSpec;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ContextImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.expectations.ExpectationsPolicy;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
import lombok.Value;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.LinkedList;
import java.util.List;

import static com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTrackerImpl.empty;
import static com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTrackerImpl.of;
import static org.valid4j.Assertive.require;

@Value
@Flogger
public final class ExecutionFactoryImpl<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements ExecutionFactory<Id, Definition, Type, Nitrite> {
    TreeContext<Id, Definition, Type> treeContext;

    @Override
    public Scoped<Id, Definition, Type, Nitrite> inScopeOf(ExecutionContext<Id, Definition, Type> parent) {
        return new Scoped<>() {
            private ExpectationsPolicy<Id> policyForType(Type type){
                if (type.isSequential()) {
                    return ExpectationsPolicy.next();
                }
                if (type.isParallel()) {
                    return ExpectationsPolicy.any();
                }
                return null;
            }

            private List<Task<Id, Definition, Type>> expectationsFor(Task<Id, Definition, Type> task){
                List<Task<Id, Definition, Type>> out;
                if (task.getType().isSequential()){
                    out = new LinkedList<>(task.getSubtasks().toList());
                    log.atFine().log("Expectations for a sequential node %s: %s", task.getId(), out);
                } else if (task.getType().isParallel()){
                    var mergeSpec = treeContext.getTaskpecFactory().forParallelNode(task);
                    log.atFine().log("Merge spec: %s", mergeSpec);
                    var mergeGenerator = treeContext.getGeneratorFactory().over(mergeSpec);
                    out =  new LinkedList<>(task.getSubtasks().filter(t -> !mergeGenerator.canReuse(t.getId())).toList());
                    log.atFine().log("Expectations for a parallel node%s: %s", task.getId(), out);
                } else {
                    out = new LinkedList<>();
                    log.atFine().log("Expectations for a leaf %s: %s", task.getId(), out);
                }
                return out;
            }

            private ExecutionContext<Id, Definition, Type> getSubtaskContext(TaskSpec<Definition, Type> taskSpec, boolean userDefinedTask){
                var idGenerator = treeContext.getGeneratorFactory().over(taskSpec);
                IncrementalHistoryTracker<Id> history = parent.history() == null ? of(empty()) : parent.history().snapshot();
                Id taskId;
                log.atFine().log("Parent ID: %s", parent.id());
                log.atFine().log("Parent expectations: %s", parent.expectations());
                if (parent.expectations().isEmpty()) {
                    taskId = idGenerator.generate();
                    log.atFine().log("Generated ID: %s", taskId);
                    if (userDefinedTask && parent.isFinished()) {
                        parent.events().bodyExtended();
                        //gradparent, because expectations are empty, so we wanna avoid empty disown journal entry
                        parent.parent().disownExpectations();
                    }
                } else {
                    var candidates = parent.policy().getCandidates(parent.expectations());
                    log.atFine().log("Candidates for reusing: %s", candidates);
                    var firstReusable = candidates.stream().filter(id -> idGenerator.canReuse(id)).findFirst();
                    log.atFine().log("First reusable candidate: %s", firstReusable);
                    if (firstReusable.isPresent()) {
                        taskId = firstReusable.get();
                        parent.reuseForSubtask(taskId);
                        log.atFine().log("Reused ID: %s", taskId);
                        log.atFine().log("Updated parent expectations: %s", parent.expectations());
                        var task = treeContext.getNitriteManagers().getTaskManager().getById(taskId);
                        return ContextImpl.<Id, Definition, Type>builder()
                            .parent(parent)
                            .task(task)
                            .history(history)
                            .emitter(treeContext.getEvents())
                            .policy(policyForType(taskSpec.getType()))
                            .expectations(expectationsFor(task))
                            .build();
                    } else {
                        taskId = idGenerator.generate();
                        log.atFine().log("Generated ID: %s", taskId);
                        if (parent.policy().noMatchingCandidatesTreatedAsConflict(parent.isFinished())){
                            parent.events().bodyChanged(parent.expectations(), taskId);
                            parent.disownExpectations();
                        } else {
                            if (userDefinedTask && parent.isFinished()) {
                                parent.events().bodyExtended();
                            }
                        }
                    }
                }

                Task<Id, Definition, Type> task = null;
                if (taskSpec.getType().isRoot()){
                    var found = treeContext.getNitriteManagers().getTaskManager().findById(taskId);
                    if (found.isPresent()) {
                        task = found.get();
                    }
                }
                if (task == null) {
                    task = SimpleTask.<Id, Definition, Type>builder()
                        .id(taskId)
                        .definition(taskSpec.getDefinition())
                        .type(taskSpec.getType())
                        .taskResolver(treeContext.getNitriteManagers().getTaskManager())
                        .build();
                    treeContext.getNitriteManagers().getTaskManager().register(task);
                }
                parent.events().defineSubtask(taskId);
                return ContextImpl.<Id, Definition, Type>builder()
                    .parent(parent)
                    .task(task)
                    .history(history)
                    .emitter(treeContext.getEvents())
                    .policy(policyForType(taskSpec.getType()))
                    .expectations(expectationsFor(task))
                    .build();
            };

            @Override
            public Execution<Id, Definition, Type> sequentialNode(TaskSpec<Definition, Type> taskSpec, NodeBody<Id, Definition, Type, Nitrite> body, boolean userDefinedTask) {
                //todo exception should say smth like "you forgot to add incorporation filter"
                require(taskSpec.getType().isSequential(), "Tried to run %s (modifier: %s) as sequential node", taskSpec, taskSpec.getType().getModifier());
                return new SequentialNodeExecution<>(treeContext, getSubtaskContext(taskSpec, userDefinedTask), body);
            }

            @Override
            public Execution<Id, Definition, Type> parallelNode(TaskSpec<Definition, Type> taskSpec, NodeBody<Id, Definition, Type, Nitrite> body, TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter, boolean userDefinedTask) {
                require(taskSpec.getType().isParallel(), "Tried to run %s (modifier: %s) as parallel node", taskSpec, taskSpec.getType().getModifier());
                return new ParallelNodeExecution<>(treeContext, getSubtaskContext(taskSpec, userDefinedTask), body, filter);
            }

            @Override
            public Execution<Id, Definition, Type> leaf(TaskSpec<Definition, Type> taskSpec, LeafBody<Id, Definition, Type, Nitrite> body, boolean userDefinedTask) {
                require(taskSpec.getType().isLeaf(), "Tried to run %s (modifier: %s) as leaf", taskSpec, taskSpec.getType().getModifier());
                return new LeafExecution<>(treeContext, getSubtaskContext(taskSpec, userDefinedTask), body);
            }
        };
    }
}
