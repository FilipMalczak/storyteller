package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.SubtaskIncorporated;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskEnded;
import com.github.filipmalczak.storyteller.impl.storage.NitriteMerger;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageFactory;
import com.github.filipmalczak.storyteller.impl.tree.TreeContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.execution.context.ExecutionContext;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.SubtaskAdapter;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.TaskExecutor;
import com.github.filipmalczak.storyteller.impl.tree.internal.executor.TaskExecutorImpl;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryDiff;
import lombok.Value;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.valid4j.Assertive.neverGetHere;
import static org.valid4j.Assertive.require;

@Value
@Flogger
public class ParallelNodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements Execution<Id, Definition, Type> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> executionContext;
    NodeBody<Id, Definition, Type, Nitrite> body;
    TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter;

    @Override
    public ExecutionContext<Id, Definition, Type> context() {
        return executionContext;
    }

    @Override
    public void run() {
        var mergeSpec = treeContext.getTaskpecFactory().forParallelNode(executionContext.task());
        var mergeGenerator = treeContext.getGeneratorFactory().over(mergeSpec);
        var storageFactory = new NitriteStorageFactory<>(
            treeContext.getNitriteManagers().getNitrite(),
            treeContext.getStorageConfig(),
            executionContext.history()
        );
        var storage = storageFactory.read(executionContext.id());
        //filter.chooseIncorporated works on full tasks, and I don't want to use Tasks as keys, thus list of pairs and not a map
        Map<Id, Task<Id, Definition, Type>> subtasks = new HashMap<>();
        Map<Id, Map<Id, HistoryDiff<Id>>> increments = new HashMap<>();
        Map<Id, Long> timestamps = new HashMap<>();
        Function<Boolean, TaskTree<Id, Definition, Type, Nitrite>> treeMaker = (forUserDefinedTasks) ->
            new SubtaskAdapter<>(
                new TaskExecutorImpl<>(treeContext, executionContext, forUserDefinedTasks),
                new TaskExecutor.Callback<Id, Definition, Type>() {
                    @Override
                    public void beforeRunning(Task<Id, Definition, Type> toStart) {
                        var id = toStart.getId();
                        subtasks.put(id, toStart);
                        timestamps.put(id, System.currentTimeMillis());
                    }

                    @Override
                    public void onFinished(Task<Id, Definition, Type> finished, Map<Id, HistoryDiff<Id>> increment) {
                        increments.put(finished.getId(), increment);
                    }
                }
            );
        var tree = treeMaker.apply(true);
        body.perform(tree, storage);
        if (!executionContext.expectations().isEmpty()) {
            executionContext.events().bodyNarrowed(executionContext.expectations());
            executionContext.disownExpectations();
        }
        executionContext.events().taskPerformed(false);
        if (executionContext.needsAmendment()) {
            executionContext.events().taskAmended();
        }
        boolean requiresNoAugmentation = true;
        //todo group definition+type as a general TaskSpec; changes to tree API, id generator and who knows what else
        var insights = storageFactory.insights(increments);
        var toIncorporate = filter
            .chooseIncorporated(
                new HashSet<Task<Id, Definition, Type>>(subtasks.values()),
                insights
            );
        var idsToIncorporate = toIncorporate.stream().map(Task::getId).collect(toSet());
        log.atFine().log("IDs to incorporate: %s", idsToIncorporate);
        var possibleMergeIds = executionContext.task().getSubtaskIds().filter(mergeGenerator::canReuse).toList();
        require(possibleMergeIds.size() < 2, "At most 1 ID should be reusable for a merge leaf");
        var optionalMergeLeaf = possibleMergeIds.stream().findFirst();
        Id mergeLeafId = null;
        if (executionContext.isFinished() || executionContext.needsAmendment()) {
            require(optionalMergeLeaf.isPresent(), "Merge leaf should have been added before finishing the task");
            var endedEntry = executionContext.task()
                .getJournalEntries()
                .filter(e -> e instanceof TaskEnded)
                .reduce((e1, e2) -> e2) //this is a trick to replace findLast()
                .get();
            var endedAtSession = endedEntry.getSession();
            var previouslyIncorporated = executionContext.task()
                .getJournalEntries()
                .filter(e -> e.getSession().equals(endedAtSession))
                .filter(e -> e instanceof SubtaskIncorporated)
                .map(e -> (SubtaskIncorporated<Id>) e)
                .map(SubtaskIncorporated::getSubtaskId)
                .filter(not(mergeGenerator::canReuse))
                .collect(toSet());
            log.atFine().log("IDs incorporated in the last session that ended the parallel node: %s", previouslyIncorporated);
            requiresNoAugmentation = previouslyIncorporated.equals(idsToIncorporate);
            if (requiresNoAugmentation){
                mergeLeafId = optionalMergeLeaf.get();
                log.atFine().log("Reusing merge leaf %s", mergeLeafId);
            } else {
                log.atFine().log("Will need augmentation");
                var added = new HashSet<>(idsToIncorporate);
                added.removeAll(previouslyIncorporated);
                var removed = new HashSet<>(previouslyIncorporated);
                removed.removeAll(idsToIncorporate);
                //todo add details to entries; enhance JournalViaListenerTests accordingly
                if (added.isEmpty()) {
                    if (removed.isEmpty()){
                        neverGetHere();
                    } else {
                        executionContext.events().nodeDeflated(removed);
                    }
                } else {
                    if (removed.isEmpty()){
                        executionContext.events().nodeInflated(added);
                    } else {
                        executionContext.events().nodeRefiltered(added, removed);
                    }
                }
                executionContext.disown(asList(optionalMergeLeaf.get()));
            }
        }
        //todo use comparing0()
        var incorporationOrder = new LinkedList<>(subtasks.values().stream().filter(toIncorporate::contains).toList());
        incorporationOrder.sort((t1, t2) -> treeContext.getMergeOrder().compare(t1.getId(), t2.getId()));
        Map<Id, HistoryDiff<Id>> mergeIncrement;
        if (mergeLeafId == null) {
            treeMaker.apply(false).execute(mergeSpec, rw -> {
                NitriteMerger merger = NitriteMerger.of(rw.documents());

                for (var incorporationSubject: incorporationOrder){
                    merger.applyChanges(
                        timestamps.get(incorporationSubject.getId()),
                        insights.into(incorporationSubject.getId()).documents()
                    );
                }
            });
            mergeLeafId = timestamps.keySet().stream().sorted(comparing(timestamps::get).reversed()).findFirst().get();
            mergeIncrement = increments.get(mergeLeafId);
        } else {
            var snap = executionContext.history().snapshot();
            snap.add(mergeLeafId, mergeLeafId, true);
            mergeIncrement = snap.getIncrement();
        }
        for (var incorporationSubject: incorporationOrder){
            var increment = increments.get(incorporationSubject.getId());
            executionContext.incorporate(incorporationSubject.getId(), increment, incorporationSubject.getType().isWriting());
        }
        executionContext.incorporate(mergeLeafId, mergeIncrement, true);
        if (!requiresNoAugmentation){
            executionContext.events().nodeAugmented();
        }

    }
}
