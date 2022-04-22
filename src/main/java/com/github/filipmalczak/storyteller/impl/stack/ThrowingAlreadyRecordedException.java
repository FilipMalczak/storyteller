package com.github.filipmalczak.storyteller.impl.stack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
class ThrowingAlreadyRecordedException extends RuntimeException {
    @Getter
    @NonNull Exception alreadyRecorded;

    public ThrowingAlreadyRecordedException(@NonNull Exception alreadyRecorded) {
        super(alreadyRecorded);
        this.alreadyRecorded = alreadyRecorded;
    }
}
