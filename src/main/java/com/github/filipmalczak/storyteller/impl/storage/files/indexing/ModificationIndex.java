package com.github.filipmalczak.storyteller.impl.storage.files.indexing;

import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.objects.ObjectRepository;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.filipmalczak.storyteller.impl.IterationUtils.toStream;
import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Flogger
public class ModificationIndex<Id extends Comparable<Id>> {
    @NonNull ObjectRepository<ModificationEvent> repository;
    @NonNull HistoryTracker<Id> history;

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public class Scope {
        @NonNull Id id;
        boolean isWriting;

        private Stream<Id> getReadingCandidates(){
            return Stream.concat(
                isWriting ? Stream.of(id) : Stream.empty(),
                history.getWritingAncestors(id)
            ).peek(i -> log.atFinest().log("Reading candidate for ID %s: %s", id, i));
        }

        public Optional<ModificationEvent> lastModificationOf(Path path){
            return getReadingCandidates()
                .flatMap( i ->
                    toStream(
                        repository.find(
                            and(
                                eq("scope", i),
                                eq("path", path.toString())
                            ),
                            FindOptions.sort("occuredAt", SortOrder.Descending).thenLimit(0, 1)
                        )
                    )
                )
                .peek(ev -> log.atFiner().log("Modification event for ID %s: %s", id, ev))
                .findFirst();
        }

        public void markWritten(Path path){
            repository.insert(new ModificationEvent<>(id, path.toString(), Modification.WRITE, ZonedDateTime.now()));
        }

        public void markDeleted(Path path){
            repository.insert(new ModificationEvent<>(id, path.toString(), Modification.DELETE, ZonedDateTime.now()));
        }

        public void purge(){
            repository.remove(eq("scope", id));
        }

        public void flush(){
            //todo ??
        }
    }

    public Scope scopeFor(Id scope, boolean isWritingScope){
        return new Scope(scope, isWritingScope);
    }
}
