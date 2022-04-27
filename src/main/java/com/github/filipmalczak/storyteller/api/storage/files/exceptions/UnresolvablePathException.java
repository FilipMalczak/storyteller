package com.github.filipmalczak.storyteller.api.storage.files.exceptions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.nio.file.Path;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnresolvablePathException extends RuntimeException {
    @Getter
    Path path;

    public UnresolvablePathException(Path path) {
        super("Path '%s' cannot be resolved! Referenced file has either been deleted or never existed!".formatted(path));
        this.path = path;
    }
}
