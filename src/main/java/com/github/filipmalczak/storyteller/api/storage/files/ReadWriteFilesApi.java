package com.github.filipmalczak.storyteller.api.storage.files;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.github.filipmalczak.storyteller.api.storage.utils.ApiUtils.withWriter;

public interface ReadWriteFilesApi extends ReadFilesApi {
    void write(Path path, Consumer<OutputStream> closure);

    default void writer(Path path, Consumer<PrintWriter> closure){
        write(path, withWriter(closure));
    }
}
