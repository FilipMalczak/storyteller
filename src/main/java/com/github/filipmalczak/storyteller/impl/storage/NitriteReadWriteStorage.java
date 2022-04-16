package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.storage.files.SimpleReadWriteFiles;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteReadWriteStorage extends NitriteReadStorage implements ReadWriteStorage {
    ReadWriteFilesApi filesApi;

    public NitriteReadWriteStorage(@NonNull NitriteStorageConfig config, @NonNull HistoryTracker tracker, @NotNull Object current) {
        super(config, tracker, current);
        this.filesApi = new SimpleReadWriteFiles(config, tracker, current);
    }

    @Override
    public ReadWriteFilesApi files(){
        return filesApi;
    }
}
;