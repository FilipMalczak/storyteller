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

import static com.github.filipmalczak.storyteller.impl.storage.utils.NitriteFsUtils.load;

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
        var latestLeaf = tracker.getLeafAncestors(current).findFirst();
        nitrite = load(config, latestLeaf);
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
