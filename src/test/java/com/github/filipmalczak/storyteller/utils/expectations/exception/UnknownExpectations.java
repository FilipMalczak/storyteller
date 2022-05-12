package com.github.filipmalczak.storyteller.utils.expectations.exception;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
public class UnknownExpectations extends AssertionError {
    @Getter Object causedBy;

    public UnknownExpectations(Object causedBy) {
        super("No expectations provided, but next element has been emitted: "+causedBy);
        this.causedBy = causedBy;
    }
}
