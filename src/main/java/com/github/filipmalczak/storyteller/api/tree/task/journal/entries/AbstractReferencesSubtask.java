package com.github.filipmalczak.storyteller.api.tree.task.journal.entries;

import com.github.filipmalczak.storyteller.api.session.Session;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.time.ZonedDateTime;

import static java.util.Arrays.asList;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public sealed class AbstractReferencesSubtask<Id extends Comparable<Id>>
    extends AbstractReferencesSubtasks<Id> implements ReferencesSubtask<Id>
    permits SubtaskDefined, SubtaskDisowned, SubtaskIncorporated {
    public AbstractReferencesSubtask(@NonNull Session session, @NonNull ZonedDateTime happenedAt, @NonNull Id reference) {
        super(session, happenedAt, asList(reference));
    }
}
