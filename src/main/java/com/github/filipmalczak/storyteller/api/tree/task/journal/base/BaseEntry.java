package com.github.filipmalczak.storyteller.api.tree.task.journal.base;

import com.github.filipmalczak.recordtuples.Pair;
import com.github.filipmalczak.storyteller.api.session.Session;
import com.github.filipmalczak.storyteller.api.tree.task.journal.annotations.Internal;
import com.github.filipmalczak.storyteller.api.tree.task.journal.entries.JournalEntry;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
@Internal
public abstract class BaseEntry {
    @NonNull Session session;
    @NonNull ZonedDateTime happenedAt;

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"(" +
            toStringElements().map(p -> p.get0()+"="+p.get1()).collect(joining(", "))+
            ')';
    }

    protected Stream<Pair<String, String>> toStringElements(){
        return Stream.of(Pair.of("session", session.getId()), Pair.of("happenedAt", happenedAt.toString()));
    }
}
