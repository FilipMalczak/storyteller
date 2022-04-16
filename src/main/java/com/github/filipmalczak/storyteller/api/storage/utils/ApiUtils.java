package com.github.filipmalczak.storyteller.api.storage.utils;

import lombok.SneakyThrows;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ApiUtils {
    enum Token { TOKEN; }

    static <T> Function<T, Token> asFunction(Consumer<T> consumer){
        return t -> { consumer.accept(t); return Token.TOKEN; };
    }

    @SneakyThrows
    static <T> T withReaderBody(InputStream stream, Function<StoredFileReader, T> function){
        try (var reader = new BufferedReader(new InputStreamReader(stream))) {
//        var reader = new BufferedReader(new InputStreamReader(stream));
        return function.apply(new StoredFileReader(reader));
        }
    }

    @SneakyThrows
    static <T> Function<InputStream, T> withReader(Function<StoredFileReader, T> function){
        return s -> withReaderBody(s, function);
    }

    static void withWriterBody(OutputStream stream, Consumer<PrintWriter> consumer){
        try (var writer = new PrintWriter(new OutputStreamWriter(stream))){
//        var writer = new PrintWriter(new OutputStreamWriter(stream));
        consumer.accept(writer);
        }
    }

    static Consumer<OutputStream> withWriter(Consumer<PrintWriter> consumer){
        return s -> withWriterBody(s, consumer);
    }
}
