package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.recordtuples.Pair;
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
import org.dizitart.no2.Nitrite;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.valid4j.Assertive.require;

@Value
public class ParallelNodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType> implements Execution<Id, Definition, Type> {
    TreeContext<Id, Definition, Type> treeContext;
    ExecutionContext<Id, Definition, Type> executionContext;
    NodeBody<Id, Definition, Type, Nitrite> body;
    TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter;

    @Override
    public ExecutionContext<Id, Definition, Type> run() {
        var mergeSpec = treeContext.getMergeSpecFactory().forParallelNode(executionContext.task());
        var mergeGenerator = treeContext.getGeneratorFactory().over(mergeSpec.definition(), mergeSpec.type());
        if (!executionContext.isStarted()) {
            executionContext.events().taskStarted();
        }
        var storageFactory = new NitriteStorageFactory<>(
            treeContext.getNitriteManagers().getNitrite(),
            treeContext.getStorageConfig(),
            executionContext.history()
        );
        var storage = storageFactory.read(executionContext.id());
        List<Pair<Task<Id, Definition, Type>, Map<Id, HistoryDiff<Id>>>> subtaskToIncrement = new ArrayList<>();
        Map<Id, Long> timestamps = new HashMap<>();
        var tree = new SubtaskAdapter<>(
            new TaskExecutorImpl<>(treeContext, executionContext),
            new TaskExecutor.Callback<Id, Definition, Type>() {
                @Override
                public void beforeRunning(Task<Id, Definition, Type> finished) {
                    timestamps.put(finished.getId(), System.currentTimeMillis());
                }

                @Override
                public void onFinished(Task<Id, Definition, Type> finished, Map<Id, HistoryDiff<Id>> increment) {
                    subtaskToIncrement.add(Pair.of(finished, increment));
                }
            }
        );
        body.perform(tree, storage);
        executionContext.events().taskPerformed(false);
        if (!executionContext.expectations().isEmpty()) {
            executionContext.events().bodyNarrowed(executionContext.expectations());
        }
        if (executionContext.needsAmendment()) {
            executionContext.events().taskAmended();
        }
        var possibleMergeIds = executionContext.task().getSubtaskIds().filter(mergeGenerator::canReuse).toList(); //todo
        require(possibleMergeIds.size() < 2, "At most 1 ID should be reusable for a merge leaf");
        var mergeLeaf = possibleMergeIds.stream().findFirst();
        var endedEntry = executionContext.task()
            .getJournalEntries()
            .filter(e -> e instanceof TaskEnded)
            .reduce((e1, e2) -> e2) //this is a trick to replace findLast()
            .get();
        var endedAtSession = endedEntry.getSession();
        //todo group definition+type as a general TaskSpec; changes to tree API, id generator and who knows what else
        var previouslyIncorporated = executionContext.task()
            .getJournalEntries()
            .filter(e -> e.getSession().equals(endedAtSession))
            .filter(e -> e instanceof SubtaskIncorporated)
            .map(e -> (SubtaskIncorporated<Id>) e)
            .map(SubtaskIncorporated::getSubtaskId)
            .filter(not(mergeGenerator::canReuse))
            .collect(toSet());
        var toIncorporate = filter
            .chooseIncorporated(
                subtaskToIncrement.stream().map(Pair::get0).collect(toSet()),
                storageFactory::insight
            );

        if (mergeLeaf.isEmpty() || !previouslyIncorporated.equals(toIncorporate)){
            if (mergeLeaf.isPresent()) {
                executionContext.disown(asList(mergeLeaf.get()));
            }
            //todo inflated and friends
            tree.execute(mergeSpec.definition(), mergeSpec.type(), rw -> {
                NitriteMerger merger = NitriteMerger.of(rw.documents());
                var incorporationOrder = new LinkedList<>(subtaskToIncrement);
                incorporationOrder.sort((t1, t2) -> treeContext.getMergeOrder().compare(t1.get0().getId(), t2.get0().getId()));
                for (var incorporationSubject: incorporationOrder){
                    //todo we only use id
                    var task = incorporationSubject.get0();
                    var increment = incorporationSubject.get1();
                    merger.applyChanges(
                        timestamps.get(task.getId()),
                        storageFactory.insight(task.getId()).documents()
                    );
                    executionContext.incorporate(task.getId(), increment);
                }
            });
            var newMerge = subtaskToIncrement.get(subtaskToIncrement.size()-1);
            executionContext.incorporate(newMerge.get0().getId(), newMerge.get1());
        }
        if (!executionContext.isFinished()) {
            executionContext.events().taskEnded();
        }
        return executionContext;
    }
}
