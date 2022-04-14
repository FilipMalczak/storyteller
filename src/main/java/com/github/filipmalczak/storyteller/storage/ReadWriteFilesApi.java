package com.github.filipmalczak.storyteller.storage;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface ReadWriteFilesApi extends ReadFilesApi {
    void write(Path path, Consumer<OutputStream> writer);
}
