package com.github.filipmalczak.storyteller.utils;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import com.github.filipmalczak.storyteller.utils.expectations.Callback;
import com.github.filipmalczak.storyteller.utils.expectations.StructuredExpectations;
import com.github.filipmalczak.storyteller.utils.expectations.condition.Condition;
import com.github.filipmalczak.storyteller.utils.expectations.exception.ExpectationNotMetException;
import com.github.filipmalczak.storyteller.utils.expectations.exception.UnknownExpectations;
import com.github.filipmalczak.storyteller.utils.expectations.exception.UnsatisfiedExpectationsLeft;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.flogger.Flogger;

import java.util.function.Predicate;

@Value
@Flogger
public class AssertiveListener implements JournalListener<JournalEntry> {
    @NonNull StructuredExpectations<ExpectedEntry, Pair<Task, JournalEntry>> expectations;

    public AssertiveListener(Object... expectations) {
        this.expectations = StructuredExpectations.<ExpectedEntry, Pair<Task, JournalEntry>>builder()
            .condition(ExpectedEntry.matches())
            .onMatch(Callback.logSuccess(log.atInfo()::log))
            .onMismatch(ctx -> {
                throw new ExpectationNotMetException(ctx);
            })
            .onLeftovers(ctx -> {
                throw new UnsatisfiedExpectationsLeft(ctx);
            })
            .onMissingInstructions(e -> {
                throw new UnknownExpectations(e);
            })
            .build()
            .expect(expectations);
    }

    @Override
    public void on(Task owner, JournalEntry entry) {
        expectations.matchNext(Pair.of(owner, entry));
    }

    public void end(){
        expectations.end();
    }

    private static String getLocation(){
        try {
            throw new RuntimeException();
        } catch (RuntimeException e){
            var frame = e.getStackTrace()[2]; //first frame is this, second is entryForTask, third is the one we look for
            return frame.getFileName()+":"+frame.getLineNumber();
        }
    }

    public static <T extends JournalEntry> ExpectedEntry<T> entryForTask(Class<T> clazz, String definition, TrivialTaskType type, Predicate<Pair<Task, T>> predicate){
        return new ExpectedEntry<>(getLocation(), clazz, definition, type, predicate);
    }

    public static <T extends JournalEntry> ExpectedEntry<T> entryForTask(Class<T> clazz, String definition, TrivialTaskType type){
        return new ExpectedEntry<>(getLocation(), clazz, definition, type, null);
    }

    public record ExpectedEntry<T extends JournalEntry>(
        String location,
        Class<T> clazz,
        String definition,
        TrivialTaskType type,
        Predicate<Pair<Task, T>> predicate
    ) {
        public static Condition<ExpectedEntry, Pair<Task, JournalEntry>> matches(){
            return Condition.<ExpectedEntry, Pair<Task, JournalEntry>>of(
                    (e, p) -> e.clazz.isInstance(p.get1()), "entry is of correct class"
                ).and(Condition.of(
                    (e, p) -> e.definition.equals(p.get0().getDefinition()), "task has correct definition")
                ).and(Condition.of(
                    (e, p) -> e.type.equals(p.get0().getType()), "task has correct type")
                ).and(Condition.ifThen(
                    Condition.of((e, p) -> e.predicate != null, "custom predicate is present"),
                    Condition.of((e, p) -> e.predicate.test(p), "predicate is satisfied")
                ));
        }
    }

}
