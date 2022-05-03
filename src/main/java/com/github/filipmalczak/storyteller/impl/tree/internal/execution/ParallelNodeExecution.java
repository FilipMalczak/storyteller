package com.github.filipmalczak.storyteller.impl.tree.internal.execution;

import com.github.filipmalczak.storyteller.api.tree.TaskTree;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.TaskType;
import com.github.filipmalczak.storyteller.api.tree.task.body.NodeBody;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.SubtaskIncorporated;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.TaskEnded;
import com.github.filipmalczak.storyteller.impl.storage.NitriteMerger;
import com.github.filipmalczak.storyteller.impl.tree.NitriteTaskTree;
import com.github.filipmalczak.storyteller.impl.tree.internal.NitriteTreeInternals;
import com.github.filipmalczak.storyteller.impl.tree.internal.ParallelSubtree;
import com.github.filipmalczak.storyteller.impl.tree.internal.TraceEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.order.SubtaskOrderingStrategy;
import com.google.common.flogger.FluentLogger;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.valid4j.Assertive.require;

@Flogger
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ParallelNodeExecution<Id extends Comparable<Id>, Definition, Type extends Enum<Type> & TaskType>
    extends AbstractTaskExecution<Id, Definition, Type, NodeBody<Id, Definition, Type, Nitrite>> {
    TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter;

    public ParallelNodeExecution(NitriteTreeInternals<Id, Definition, Type> internals, Definition definition, Type type, NodeBody<Id, Definition, Type, Nitrite> body, SubtaskOrderingStrategy<Id> orderingStrategy, TaskTree.IncorporationFilter<Id, Definition, Type, Nitrite> filter, boolean recordIncorporateToParent) {
        super(internals, definition, type, body, orderingStrategy, recordIncorporateToParent);
        this.filter = filter;
    }

    @Override
    protected FluentLogger getLogger() {
        return log;
    }

    @Override
    protected void validateContract() {
        validateSubtaskContract();
    }

    @Override
    protected void handleRunning() {
        internals.events().taskPerformed(thisTask, false);
        getLogger().atFine().log("Running instructions of task %s (as always for nodes)", id);
        runInstructions();

    }

    private void runInstructions() {
        var storage = internals.storageFactory().parallelRead(id);
        var newTrace = new LinkedList<>(internals.trace());
        var expectations = new LinkedList<>(thisTask.getSubtaskIds().toList());
        var mergeSpec = internals.mergeSpecFactory().forParallelNode(thisTask);
        var mergeIdGenerator = internals.idGeneratorFactory().over(mergeSpec.definition(), mergeSpec.type());
        boolean augmented = false;
        Id mergeLeafId = null;
        Set<Id> previouslyIncorporated = null;
        if (finished) {
            mergeLeafId = expectations.remove(expectations.size()-1);
            var endedEntry = thisTask
                .getJournalEntries()
                .filter(e -> e instanceof TaskEnded)
                .reduce((e1, e2) -> e2) //this is a trick to replace findLast()
                .get();
            var endedAtSession = endedEntry.getSession();
            //todo group definition+type as a general TaskSpec; changes to tree API, id generator and who knows what else
            previouslyIncorporated = thisTask
                .getJournalEntries()
                .filter(e -> e.getSession().equals(endedAtSession))
                .filter(e -> e instanceof SubtaskIncorporated)
                .map(e -> (SubtaskIncorporated<Id>) e)
                .map(SubtaskIncorporated::getSubtaskId)
                .filter(not(mergeIdGenerator::canReuse))
                .collect(toSet());
            log.atFine().log("Previously incorporated subtask IDs: %s", previouslyIncorporated);
        }
        var newEntry = new TraceEntry<>(thisTask, null, expectations, storage);
        getLogger().atFine().log("Pushing new trace entry: %s", newEntry);
        newTrace.addFirst(newEntry);

        var subtree = new ParallelSubtree<>(
            internals.managers(),
            internals.history(),
            internals.storageFactory().getConfig(),
            internals.idGeneratorFactory(),
            internals.mergeSpecFactory(),
            newTrace
        );
        body.perform(subtree, storage);
        if (!newEntry.getExpectedSubtaskIds().isEmpty()) {
            log.atFine().log("After running the node some subtasks are still expected; disowning them, as the node has narrowed");
            internals.events().bodyShrunk(
                thisTask,
                newEntry
                    .getExpectedSubtaskIds()
            );
            disownExpectedUpTheTrace(newTrace);
        }
        //well, all besides the merge leaf
        var allSubtasks = thisTask.getSubtasks().filter(t -> !mergeIdGenerator.canReuse(t.getId())).toList();
        var toIncorporate = filter.chooseIncorporated(new HashSet<>(allSubtasks), subtree.getInsights());
        var idsToIncorporate = toIncorporate.stream().map(Task::getId).collect(toSet());
        log.atFine().log("Subtask IDs to incorporate: %s", idsToIncorporate);
        //todo write down order of shrunk/refiltered/inflated/deflated/etc in javadocs
        if (finished) {
            if (!idsToIncorporate.equals(previouslyIncorporated)) {
                log.atFine().log("Parallel node needs augmentation, disowning merge leaf as well as following expected tasks");
                getFriend().disownSubtask(thisTask, mergeLeafId);
                disownExpectedUpTheTrace();
                finished = false;
                augmented = true;
            }
            //todo weird construct, but it looks clearer (bodies of ifs could be merged)
            if (augmented) {
                var newlyIncorporated = new HashSet<>(idsToIncorporate);
                newlyIncorporated.removeAll(previouslyIncorporated);
                var notIncorporatedAnymore = new HashSet<>(previouslyIncorporated);
                notIncorporatedAnymore.removeAll(idsToIncorporate);
                if (newlyIncorporated.isEmpty()) {
                    //single they are not equal, then notIncorporatedAnymore cannot be empty;
                    // thus some disappeared, none appeared
                    internals.events().nodeDeflated(thisTask);
                } else {
                    if (notIncorporatedAnymore.isEmpty()) {
                        //some appeared, none disappeared
                        internals.events().nodeInflated(thisTask);
                    } else {
                        //some appeared, some disappeared
                        internals.events().nodeRefiltered(thisTask);
                    }
                }
                internals.events().nodeAugmented(thisTask);
            }
        }
        if (!finished || augmented) {
            newTrace = new LinkedList<>(internals.trace());
            newEntry = new TraceEntry<>(thisTask, null, new LinkedList<>(), storage);
            newTrace.addFirst(newEntry);
            require(mergeSpec.type().isLeaf(), "Merge nodes must be leaves");
            log.atFine().log("Executing merge leaf");
            new NitriteTaskTree<>(
                internals.managers(),
                internals.history(),
                internals.storageFactory().getConfig(),
                internals.idGeneratorFactory(),
                internals.mergeSpecFactory(),
                newTrace,
                true
            ).execute(
                mergeSpec.definition(),
                mergeSpec.type(),
                rw -> {
                    NitriteMerger merger = NitriteMerger.of(rw.documents());
                    //todo sort by definition.toString to keep reproducable order?
                    for (var incorporationSubject: idsToIncorporate){
                        internals.history().apply(subtree.getHistory(incorporationSubject).getIncrement());
                        merger.applyChanges(
                            subtree.getStartTimestamp(incorporationSubject),
                            subtree.getInsights().into(incorporationSubject).documents()
                        );
                        internals.events().subtaskIncorporated(thisTask, incorporationSubject);
                    }
                }
            );
        } else {
            log.atFine().log("Inorporated subtasks set didn't change; applying changes in-memory and reusing merge leaf");
            //todo sort by definition.toString to keep reproducable order?
            for (var incorporationSubject: idsToIncorporate){
                internals.history().apply(subtree.getHistory(incorporationSubject).getIncrement());
                internals.events().subtaskIncorporated(thisTask, incorporationSubject);
            }
        }

    }

}
