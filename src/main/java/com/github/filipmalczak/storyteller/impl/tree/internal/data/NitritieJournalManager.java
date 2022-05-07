package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.model.JournalEntryData;
import com.github.filipmalczak.storyteller.impl.tree.internal.data.serialization.JournalEntrySerializer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.ObjectRepository;

import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.IterationUtils.toStream;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Setter(AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Flogger
public class NitritieJournalManager<Id extends Comparable<Id>> implements JournalEntryManager<Id> {
    @NonNull ObjectRepository<JournalEntryData> repository;
    @NonNull JournalEntrySerializer serializer;
    @NonNull SessionManager sessionManager;

    @Override
    public void record(TaskEntry<Id>... entries) {
        log.atFiner().log("Request to record: %s", entries);
        JournalEntryData<Id>[] toInsert = new JournalEntryData[entries.length];
        for (int i = 0; i< entries.length; ++i){
            var e = entries[i];
            e.task().record(e.entry());
            toInsert[i] = serializer.fromEntry(e.task(), e.entry());
        }
        log.atFiner().log("Inserting new entries: %s", toInsert);
        repository.insert(toInsert);
        for (var e: entries) {
            sessionManager.emit(e.task(), e.entry());
        }
    }

    @Override
    public Stream<JournalEntry> findById(Id taskId) {
        return toStream(
            repository
                .find(
                    eq("taskId", taskId),
                    FindOptions.sort("happenedAt", SortOrder.Ascending)
                )
        ).map(serializer::toEntry);
    }
}
