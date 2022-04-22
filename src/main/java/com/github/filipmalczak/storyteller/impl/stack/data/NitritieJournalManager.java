package com.github.filipmalczak.storyteller.impl.stack.data;

import com.github.filipmalczak.storyteller.api.stack.task.Task;
import com.github.filipmalczak.storyteller.api.stack.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.stack.data.model.JournalEntryData;
import com.github.filipmalczak.storyteller.impl.stack.data.serialization.JournalEntrySerializer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.Comparator;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.IterationUtils.toStream;
import static java.util.Comparator.comparing;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Setter(AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NitritieJournalManager<TaskId extends Comparable<TaskId>> implements JournalEntryManager<TaskId> {
    @NonNull ObjectRepository<JournalEntryData> repository;
    @NonNull JournalEntrySerializer serializer;

    @Override
    public void record(TaskEntry<TaskId>... entries) {
        JournalEntryData<TaskId>[] toInsert = new JournalEntryData[entries.length];
        for (int i = 0; i< entries.length; ++i){
            var e = entries[i];
            e.task().record(e.entry());
            toInsert[i] = serializer.fromEntry(e.task(), e.entry());
        }
        repository.insert(toInsert);
    }

    @Override
    public Stream<JournalEntry> findByTaskId(TaskId taskId) {
        return toStream(repository.find(eq("taskId", taskId))).sorted(comparing(JournalEntryData::getHappenedAt)).map(serializer::toEntry);
    }
}
