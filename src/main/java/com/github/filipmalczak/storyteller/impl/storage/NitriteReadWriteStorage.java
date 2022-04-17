package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.storage.files.SimpleReadWriteFiles;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Exporter;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteReadWriteStorage<Id extends Comparable<Id>> extends NitriteReadStorage<Id> implements ReadWriteStorage<Nitrite> {
    SimpleReadWriteFiles filesApi;

    public NitriteReadWriteStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NotNull Id current) {
        super(config, tracker, current);
        this.filesApi = new SimpleReadWriteFiles<Id>(config, tracker, current);
    }

    @Override
    public ReadWriteFilesApi files(){
        return filesApi;
    }

    public void flush(){
        var exporter = Exporter.of(nitrite);
        exporter.exportTo(getNitriteFile(current));
    }

    public void purge(){
        filesApi.purge();
        var currentNitriteFile = getNitriteFile(current);
        if (currentNitriteFile.exists())
            currentNitriteFile.delete();
    }
}
;