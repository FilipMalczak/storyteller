package com.github.filipmalczak.storyteller.api.storage.files;

import com.github.filipmalczak.storyteller.api.storage.utils.StoredFileReader;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.filipmalczak.storyteller.api.storage.utils.ApiUtils.asFunction;
import static com.github.filipmalczak.storyteller.api.storage.utils.ApiUtils.withReader;

public interface ReadFilesApi {
    boolean exists(Path path);

    //todo add forEachInGlob(String glob, BiFunction<Path, InputStream, T> and default for biconsumer

    /**
     * Throws if doesnt exist
     */
    <T> T read(Path path, Function<InputStream, T> closure);

    /**
     * Throws if doesnt exist
     */
    default void read(Path path, Consumer<InputStream> closure){
        read(path, asFunction(closure));
    }

    default <T> T reader(Path path, Function<StoredFileReader, T> closure){
        return read(path, withReader(closure));
    }

    default String readAll(Path path){
        return read(path, withReader(r -> r.readAll())).trim();
    }
}
