package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.flogger.Flogger;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.valid4j.Assertive.require;

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

    public void purge(){
        fileScope.purge();
    }
}
