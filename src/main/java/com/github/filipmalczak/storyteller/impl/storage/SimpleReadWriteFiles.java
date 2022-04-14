package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.storage.ReadWriteFilesApi;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.valid4j.Assertive.require;

public class SimpleReadWriteFiles<Id> extends SimpleReadFiles<Id> implements ReadWriteFilesApi {

    public SimpleReadWriteFiles(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current) {
        super(config, tracker, current);
        ensureEmptyCurrent();
    }

    @SneakyThrows
    private void ensureEmptyCurrent(){
        Path currentWorkspace = getFilesPath().resolve(config.getSerializer().toString(current));
        File currentDir = currentWorkspace.toFile();
        if (currentDir.exists()){
            require(currentDir.isDirectory(), "Preexisting task workspace must be a directory");
            deleteDirectory(currentDir);
        }
        currentDir.mkdirs();
    }

    @Override
    @SneakyThrows
    public void write(Path path, Consumer<OutputStream> writer) {
        var f = getFilesPath().resolve(config.getSerializer().toString(current)).resolve(path).toFile();
        var stream = new FileOutputStream(f);
        try {
            writer.accept(stream);
        } finally {
            stream.close();
        }
    }
}
