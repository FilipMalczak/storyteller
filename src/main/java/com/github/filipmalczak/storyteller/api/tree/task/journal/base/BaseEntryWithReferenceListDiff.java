package com.github.filipmalczak.storyteller.api.tree.task.journal.base;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.Internal;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Internal
public abstract class BaseEntryWithReferenceListDiff<Id extends Comparable<Id>> extends BaseEntry {
    @NonNull List<Id> increment;
    @NonNull List<Id> decrement;

    public BaseEntryWithReferenceListDiff(@NonNull LazySession session, @NonNull ZonedDateTime happenedAt, @NonNull List<Id> increment, @NonNull List<Id> decrement) {
        super(session, happenedAt);
        this.increment = increment;
        this.decrement = decrement;
    }

    public Stream<Id> getIncrement(){
        return increment.stream();
    }

    public Stream<Id> getDecrement(){
        return decrement.stream();
    }

    public Stream<Id> getReferences() {
        return Stream.concat(getIncrement(), getDecrement());
    }

    @Override
    protected Stream<Pair<String, String>> toStringElements(){
        return Stream.concat(
            super.toStringElements(),
            Stream.of(
                Pair.of(toStringIncrementAlias()+".size", ""+increment.size()),
                Pair.of(toStringDecrementAlias()+".size", ""+decrement.size())
            )
        );
    }

    protected abstract String toStringIncrementAlias();
    protected abstract String toStringDecrementAlias();
}
