package com.github.filipmalczak.storyteller.api.storage.files.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AbsolutePathException extends RuntimeException {
    @Getter Path path;

    public AbsolutePathException(Path path) {
        super("Path '%s' is absolute! Only relative paths are supported by storage!".formatted(path));
        this.path = path;
    }
}
