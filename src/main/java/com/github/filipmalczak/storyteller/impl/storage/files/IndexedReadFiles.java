package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.api.storage.files.exceptions.UnresolvablePathException;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Flogger
public class IndexedReadFiles<Id extends Comparable<Id>> implements ReadFilesApi {
    @NonNull FileIndex<Id>.Scope fileScope;

    @Override
    public boolean exists(Path path) {
        return fileScope.exists(path);
    }

    @SneakyThrows
    private static <T> T withInputStream(File file, Function<InputStream, T> closure){
        try (var s = new FileInputStream(file)) {
            return closure.apply(s);
        }
    }

    @Override
    public <T> T read(Path path, Function<InputStream, T> closure) {
        return fileScope.forReading(
            path,
            f -> withInputStream(f, closure),
            () -> {
                throw new UnresolvablePathException(path);
            }
        );
    }
}
