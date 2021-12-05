package com.github.filipmalczak.storyteller.impl.jgit.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;

public class FSUtils {
    @SneakyThrows
    public static String readFile(File file){
        return Files.readString(file.toPath());
    }

    @SneakyThrows
    public static void writeFile(File file, String txt){
        Files.writeString(file.toPath(), txt);
    }
}
