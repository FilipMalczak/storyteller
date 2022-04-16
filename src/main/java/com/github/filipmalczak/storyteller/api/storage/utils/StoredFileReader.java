package com.github.filipmalczak.storyteller.api.storage.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.h2.util.IOUtils;

import java.io.BufferedReader;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoredFileReader {
    BufferedReader reader;

    @SneakyThrows
    public String read(int chars){
        char[] result = new char[chars];
        reader.read(result, 0, chars);
        return String.copyValueOf(result);
    }

    public String read(){
        return read(1);
    }

    @SneakyThrows
    public String readLine(){
        return reader.readLine();
    }

    @SneakyThrows
    public String readAll(){
        return IOUtils.readStringAndClose(reader,-1);
    }
}
