package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.api.storage.files.exceptions.UnresolvablePathException;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

@Flogger
public class IndexedReadWriteFiles<Id extends Comparable<Id>> extends IndexedReadFiles<Id> implements ReadWriteFilesApi {
    IndexedReadWriteFiles(FileIndex<Id>.@NonNull Scope fileScope) {
        super(fileScope);
    }

    @SneakyThrows
    private static void withOutputStream(File file, Consumer<OutputStream> closure){
        try (var s = new FileOutputStream(file)) {
            closure.accept(s);
        }
    }

    @Override
    public void write(Path path, Consumer<OutputStream> closure) {
        fileScope.forWriting(path, f -> withOutputStream(f, closure));
    }

    @Override
    public void delete(Path path) {
        fileScope.delete(path, () -> { throw new UnresolvablePathException(path); });
    }

    public void purge(){
        fileScope.purge();
    }
}
