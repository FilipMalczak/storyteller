package com.github.filipmalczak.storyteller.api.tree.task.journal.base;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.Internal;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Internal
public abstract class BaseEntryWithOneAndManyReferences<Id extends Comparable<Id>> extends BaseEntry {
    @NonNull Id reference;
    @Getter(value = AccessLevel.NONE) @NonNull List<Id> references;

    public BaseEntryWithOneAndManyReferences(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt, @NonNull Id reference, @NonNull List<Id> references) {
        super(session, happenedAt);
        this.reference = reference;
        this.references = references;
    }

    public Stream<Id> getReferences(){
        return references.stream();
    }

    @Override
    protected Stream<Pair<String, String>> toStringElements(){
        return Stream.concat(
            super.toStringElements(),
            Stream.of(
                Pair.of(toStringReferenceAlias()+".size", ""+reference),
                Pair.of(toStringReferencesAlias()+".size", ""+references.size())
            )
        );
    }

    protected abstract String toStringReferencesAlias();

    protected abstract String toStringReferenceAlias();
}
