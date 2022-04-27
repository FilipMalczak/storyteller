package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.files.IndexedReadFiles;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Exporter;

import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.getNitriteFile;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class NitriteParallelStorage<Id extends Comparable<Id>> extends NitriteReadStorage<Id> {
    NitriteMerger merger;

    NitriteParallelStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current, @NonNull IndexedReadFiles<Id> readFiles) {
        super(config, tracker, current, readFiles);
        merger = NitriteMerger.of(nitrite);
    }

    @Override
    public void reload() {
        //in case of parallel nodes, disable explicit reloading, so incorporated changes arent overwritten
    }

    public void incorporate(long pivot, Nitrite other){
        merger.applyChanges(pivot, other);
    }

    public void flush(){
        var exporter = Exporter.of(nitrite);
        exporter.exportTo(getNitriteFile(config, current));
    }
}
