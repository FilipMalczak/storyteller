package com.github.filipmalczak.storyteller.utils;

import com.github.filipmalczak.storyteller.api.session.listener.JournalListener;
import com.github.filipmalczak.storyteller.api.tree.task.Task;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.testimpl.TrivialTaskType;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.flogger.Flogger;
import org.opentest4j.AssertionFailedError;

import java.util.Iterator;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Value
@Flogger
public class AssertiveListener implements JournalListener<JournalEntry> {
    @NonNull Iterator<ExpectedEntry> expectations;

    public AssertiveListener(ExpectedEntry... expectations) {
        this.expectations = asList(expectations).iterator();
    }

    @Override
    public void on(Task owner, JournalEntry entry) {
        expectations.next().match(owner, entry);
    }

    public void end(){
        assertFalse(expectations.hasNext(), "Some expectations were left");
    }

    public static <T extends JournalEntry> ExpectedEntry<T> expect(Class<T> clazz, String definition, TrivialTaskType type, Predicate<T> details){
        return new ExpectedEntry<>(clazz, definition, type, details);
    }

    public static <T extends JournalEntry> ExpectedEntry<T> expect(Class<T> clazz, String definition, TrivialTaskType type){
        return expect(clazz, definition, type, e -> true);
    }

    public record ExpectedEntry<T extends JournalEntry>(
        Class<T> clazz,
        String definition,
        TrivialTaskType type,
        Predicate<T> details
    ) {
        void match(Task task, JournalEntry entry){
            try {
                assertTrue(clazz.isInstance(entry), "Expected entry class is " + clazz.getSimpleName() + " while the actual is " + entry.getClass().getSimpleName());
                assertThat(task.getDefinition(), equalTo(definition));
                assertThat(task.getType(), equalTo(type));
                assertTrue(details.test((T) entry));
            } catch (AssertionFailedError e){
                log.atInfo().log("Actual: task.definition=%s, task.type=%s, entry=%s", task.getDefinition(), task.getType(), entry);
                throw e;
            }
        }
    }


}
