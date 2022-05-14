package com.github.filipmalczak.storyteller.api.tree.task.journal.base;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.Internal;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Internal
public abstract class BaseEntryWithOneReference<Id extends Comparable<Id>> extends BaseEntry {
    @NonNull Id reference;

    public BaseEntryWithOneReference(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull Id reference) {
        super(session, happenedAt);
        this.reference = reference;
    }

    @Override
    protected Stream<Pair<String, String>> toStringElements(){
        return Stream.concat(super.toStringElements(), Stream.of(Pair.of(toStringReferenceAlias()+".size", ""+reference)));
    }

    protected abstract String toStringReferenceAlias();
}
