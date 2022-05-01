package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.ReadWriteStorage;
import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.files.IndexedReadWriteFiles;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Exporter;

import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.getNitriteFile;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NitriteReadWriteStorage<Id extends Comparable<Id>> extends AbstractNitriteStorage<Id> implements ReadWriteStorage<Nitrite> {
    @NonNull IndexedReadWriteFiles filesApi;

    NitriteReadWriteStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current, @NonNull IndexedReadWriteFiles filesApi) {
        super(config, tracker, current);
        this.filesApi = filesApi;
    }

    @Override
    public ReadWriteFilesApi files(){
        return filesApi;
    }

    public void flush(){
        var exporter = Exporter.of(nitrite);
        exporter.exportTo(getNitriteFile(config, current));
    }

    public void purge(){
        filesApi.purge();
        var currentNitriteFile = getNitriteFile(config, current);
        if (currentNitriteFile.exists())
            currentNitriteFile.delete();
    }
}
;