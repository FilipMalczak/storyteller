package com.github.filipmalczak.storyteller.impl.tree.internal.data;

import com.github.filipmalczak.storyteller.api.session.events.SessionEvent;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import com.github.filipmalczak.storyteller.impl.nextgen.SessionEventData;
import com.github.filipmalczak.storyteller.impl.nextgen.SessionEventSerializer;
import com.github.filipmalczak.storyteller.impl.nextgen.TaskEntry;
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
public class NitriteEventsPersistence<Id extends Comparable<Id>> implements EventsPersistence<Id> {
    @NonNull ObjectRepository<JournalEntryData> journalRepository;
    @NonNull ObjectRepository<SessionEventData> sessionRepository;
    @NonNull JournalEntrySerializer journalSerializer;
    @NonNull SessionEventSerializer sessionSerializer;

    @Override
    public void persist(TaskEntry<Id>... entries) {
        log.atFiner().log("Request to record: %s", entries);
        JournalEntryData<Id>[] toInsert = new JournalEntryData[entries.length];
        for (int i = 0; i< entries.length; ++i){
            var e = entries[i];
            e.task().record(e.entry());
            toInsert[i] = journalSerializer.fromEntry(e.task(), e.entry());
        }
        log.atFiner().log("Inserting new entries: %s", toInsert);
        journalRepository.insert(toInsert);
    }

    @Override
    public void persist(SessionEvent event) {
        sessionRepository.insert(sessionSerializer.serialize(event));
    }

    @Override
    public Stream<JournalEntry> findEntriesByTaskId(Id taskId) {
        return toStream(
            journalRepository
                .find(
                    eq("taskId", taskId),
                    FindOptions.sort("happenedAt", SortOrder.Ascending)
                )
        ).map(journalSerializer::toEntry);
    }
}
