package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.SimpleTask;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.LeafBody;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ContextImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.expectations.ExpectationsPolicy;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTracker;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTrackerImpl;
import lombok.Value;
import org.dizitart.no2.Nitrite;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.github.filipmalczak.storyteller.api.tree.task.journal.EntryType.toType;
import static com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTrackerImpl.empty;
import static com.github.filipmalczak.storyteller.impl.tree.internal.history.IncrementalHistoryTrackerImpl.of;
import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;
import static org.valid4j.Assertive.require;

@Value
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
                if (task.getType().isSequential()){
                    return new LinkedList<>(task.getSubtasks().toList());
                }
                if (task.getType().isParallel()){
                    var mergeSpec = treeContext.getMergeSpecFactory().forParallelNode(task);
                    var mergeGenerator = treeContext.getGeneratorFactory().over(mergeSpec.definition(), mergeSpec.type());
                    return new LinkedList<>(task.getSubtasks().filter(t -> !mergeGenerator.canReuse(t.getId())).toList());
                }
                return new LinkedList<>();
            }

            private ExecutionContext<Id, Definition, Type> getSubtaskContext(Definition definition, Type type){
                var idGenerator = treeContext.getGeneratorFactory().over(definition, type);
                IncrementalHistoryTracker<Id> history = parent.history() == null ? of(empty()) : parent.history().snapshot();
                Id taskId;

                if (parent.expectations().isEmpty() || parent.id() == null) {
                    taskId = idGenerator.generate();
                    if (parent.isFinished()) {
                        parent.events().bodyExtended();
                        parent.parent().disownExpectations();
                    }
                } else {
                    var candidates = parent.policy().getCandidates(parent.expectations());
                    var firstReusable = candidates.stream().filter(id -> idGenerator.canReuse(id)).findFirst();

                    if (firstReusable.isPresent()) {
                        taskId = firstReusable.get();
                        var task = treeContext.getNitriteManagers().getTaskManager().getById(taskId);
                        return ContextImpl.<Id, Definition, Type>builder()
                            .parent(parent)
                            .task(task)
                            .history(history)
                            .emitter(treeContext.getEvents())
                            .policy(policyForType(type))
                            .expectations(expectationsFor(task))
                            .build();
                    } else {
                        taskId = idGenerator.generate();
                        if (parent.policy().noMatchingCandidatesTreatedAsConflict(parent.isFinished())){
                            parent.events().bodyChanged(parent.expectations(), taskId);
                            parent.disownExpectations();
                        }
                    }
                }

                Task<Id, Definition, Type> task = null;
                if (type.isRoot()){
                    var found = treeContext.getNitriteManagers().getTaskManager().findById(taskId);
                    if (found.isPresent()) {
                        task = found.get();
                    }
                }
                if (task == null) {
                    task = SimpleTask.<Id, Definition, Type>builder()
                        .id(taskId)
                        .definition(definition)
                        .type(type)
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
                    .policy(policyForType(type))
                    .expectations(expectationsFor(task))
                    .build();
            };

            @Override
            public Execution<Id, Definition, Type> sequentialNode(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body) {
                return new SequentialNodeExecution<>(treeContext, getSubtaskContext(definition, type), body);
            }

            @Override
            public Execution<Id, Definition, Type> parallelNode(Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body, TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter) {
                return new ParallelNodeExecution<>(treeContext, getSubtaskContext(definition, type), body, filter);
            }

            @Override
            public Execution<Id, Definition, Type> leaf(Definition definition, Type type, LeafBody<Id, Definition, Type, Nitrite> body) {
                return new LeafExecution<>(treeContext, getSubtaskContext(definition, type), body);
            }
        };
    }
}
