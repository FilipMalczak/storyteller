package com.github.filipmalczak.storyteller.api.tree.task.journal.base;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.Internal;
import com.github.filipmalczak.storyteller.api.tree.task.journal.markers.structural.ReferencesTasks;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Internal
public abstract class BaseEntryWithReferences<Id extends Comparable<Id>> extends BaseEntry implements ReferencesTasks<Id> {
    @NonNull List<Id> references;

    public BaseEntryWithReferences(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> references) {
        super(session, happenedAt);
        this.references = unmodifiableList(references);
    }

    @Override
    public Stream<Id> getReferences() {
        return references.stream();
    }

    @Override
    protected Stream<Pair<String, String>> toStringElements(){
        return Stream.concat(super.toStringElements(), Stream.of(Pair.of(toStringReferencesAlias()+".size", ""+references.size())));
    }

    protected abstract String toStringReferencesAlias();
}
