package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.storage.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.storage.ReadWriteStorage;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class NitriteReadWriteStorage extends NitriteReadStorage implements ReadWriteStorage {


    public NitriteReadWriteStorage(@NonNull NitriteStorageConfig config, @NonNull HistoryTracker tracker, @NotNull Object current) {
        super(config, tracker, current);
    }

    @Override
    public ReadWriteFilesApi files(){
        return new SimpleReadWriteFiles(config, tracker, current);
    }
}
