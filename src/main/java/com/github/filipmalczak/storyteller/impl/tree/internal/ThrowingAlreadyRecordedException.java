package com.github.filipmalczak.storyteller.impl.tree.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * Used, so that we can differentiate between exception that has just been thrown and the one that originates from a subtask.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
public class ThrowingAlreadyRecordedException extends RuntimeException {
    @Getter
    @NonNull Exception alreadyRecorded;

    public ThrowingAlreadyRecordedException(@NonNull Exception alreadyRecorded) {
        super(alreadyRecorded);
        this.alreadyRecorded = alreadyRecorded;
    }
}
