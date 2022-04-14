package com.github.filipmalczak.storyteller.storage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ReadFilesApi {
    boolean exists(Path path);

    //todo add forEachInGlob(String glob, BiFunction<Path, InputStream, T> and default for biconsumer

    /**
     * Throws if doesnt exist
     */
    <T> T read(Path path, Function<InputStream, T> reader);

    /**
     * Throws if doesnt exist
     */
    default void read(Path path, Consumer<InputStream> reader){
        read(path, is -> { reader.accept(is); return this; });
    }
}
