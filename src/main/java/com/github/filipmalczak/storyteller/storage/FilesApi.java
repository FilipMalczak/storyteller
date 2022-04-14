package com.github.filipmalczak.storyteller.storage;

import com.github.filipmalczak.storyteller.storage.envelope.FileEnvelope;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FilesApi {
    boolean exists(Path path);

    //todo should we mark deleted in fs? or keep a nitrite collection for that?
    //void delete(String path);

    void write(Path path, Consumer<OutputStream> writer);

    <T> T read(Path path, Function<InputStream, T> reader);

    default void read(Path path, Consumer<InputStream> reader){
        read(path, is -> { reader.accept(is); return this; });
    }
}
