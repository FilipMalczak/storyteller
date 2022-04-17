package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.storage.ReadStorage;
import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.storage.files.SimpleReadFiles;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Importer;

import java.io.File;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class NitriteReadStorage<Id extends Comparable<Id>> implements ReadStorage<Nitrite> {
    @NonNull NitriteStorageConfig<Id> config;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull Id current;
    @NonFinal Nitrite nitrite;

    public NitriteReadStorage(@NonNull NitriteStorageConfig<Id> config, @NonNull HistoryTracker<Id> tracker, @NonNull Id current) {
        this.config = config;
        this.tracker = tracker;
        this.current = current;
        loadNitrite();
    }

    public void reload(){
        loadNitrite();
    }

    protected void loadNitrite(){
        var latestLeaf = tracker.getLeaves(current).findFirst();
        var builder = Nitrite.builder();
        if (config.isEnableNo2OffHeapStorage())
            builder = builder.enableOffHeapStorage();
        nitrite = builder.openOrCreate();
        if (latestLeaf.isPresent()) {
            var importer = Importer.of(nitrite);
            importer.importFrom(getNitriteFile(latestLeaf.get()));
        }
    }

    protected File getNitriteFile(Id id){
        return config
            .getDataStorage()
            .resolve("nosql")
            .resolve(config.getSerializer().toString(id))
            .resolve("data.json")
            .toFile();
    }

    @Override
    public ReadFilesApi files() {
        return new SimpleReadFiles(config, tracker, current);
    }

    @Override
    public Nitrite documents() {
        return nitrite;
    }
}
