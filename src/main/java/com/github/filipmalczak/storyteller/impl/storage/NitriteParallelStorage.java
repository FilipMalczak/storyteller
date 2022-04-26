package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.files.SimpleReadFiles;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Exporter;

import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.getNitriteFile;
import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.load;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class NitriteParallelStorage<Id extends Comparable<Id>> extends NitriteReadStorage<Id> {
    NitriteMerger merger;

    public NitriteParallelStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current) {
        super(config, tracker, current);
        merger = NitriteMerger.of(nitrite);
    }

    @Override
    public void reload() {
        //in case of parallel nodes, disable explicit reloading, so incorporated changes arent overwritten
    }

    @Override
    public ReadFilesApi files() {
        return new SimpleReadFiles(config, tracker, current);
    }

    @Override
    public Nitrite documents() {
        return nitrite;
    }

    public void incorporate(long pivot, Nitrite other){
        merger.applyChanges(pivot, other);
    }


    public void flush(){
        var exporter = Exporter.of(nitrite);
        exporter.exportTo(getNitriteFile(config, current));
    }
}
