package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.valid4j.Assertive.require;

@Flogger
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimpleReadWriteFiles<Id extends Comparable<Id>> extends SimpleReadFiles<Id> implements ReadWriteFilesApi {
    File currentDir;

    protected Stream<Id> getLeavesHistory(){
        return Stream.concat(Stream.of(current), super.getLeavesHistory());
    }

    public SimpleReadWriteFiles(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current) {
        super(config, tracker, current);
        Path currentWorkspace = getFilesPath().resolve(config.getSerializer().toString(current));
        currentDir = currentWorkspace.toFile();
        ensureEmptyCurrent();
    }

    @SneakyThrows
    private void ensureEmptyCurrent(){
        if (currentDir.exists()){
            require(currentDir.isDirectory(), "Preexisting task workspace must be a directory");
            deleteDirectory(currentDir);
            log.atFine().log("Purging existing workspace for task %s; content was: %s", current, currentDir.list());
//            require(currentDir.list().length == 0, "Preexisting task workspace must be empty");
        }
        currentDir.mkdirs();
    }

    @Override
    @SneakyThrows
    public void write(Path path, Consumer<OutputStream> writer) {
        var f = getFilesPath().resolve(config.getSerializer().toString(current)).resolve(path).toFile();
        log.atFine().log("File for path %s: %s", path, f);
        try (var stream = new FileOutputStream(f)) {
            writer.accept(stream);
        }
    }

    @SneakyThrows
    public void purge(){
        deleteDirectory(currentDir);
    }
}
