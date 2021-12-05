package com.github.filipmalczak.storyteller.impl.jgit.storage;

import com.github.filipmalczak.storyteller.impl.jgit.storage.index.IndexFile;
import lombok.Value;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

@Value
public class Workspace {
    String name;
    File workingDir;

    public File file(String... path){
        //todo fugly
        return new File(workingDir, Arrays.stream(path).collect(Collectors.joining("/")));
    }
}