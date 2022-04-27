package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.api.storage.files.ReadWriteFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import com.github.filipmalczak.storyteller.impl.storage.files.indexing.ModificationEvent;
import com.github.filipmalczak.storyteller.impl.storage.files.indexing.ModificationIndex;
import com.github.filipmalczak.storyteller.impl.tree.internal.history.HistoryTracker;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.dizitart.no2.Nitrite;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilesApiFactory<Id extends Comparable<Id>> {
    //read, write; insight = read with isWriting=true
    @NonNull FileIndex<Id> fileIndex;

    public FilesApiFactory(@NonNull Nitrite db, @NonNull HistoryTracker<Id> historyTracker, @NonNull NitriteStorageConfig<Id> config) {
        fileIndex = new FileIndex<>(
            config.getDataStorage(),
            new ModificationIndex<>(
                db.getRepository(ModificationEvent.class),
                historyTracker
            ),
            config.getSerializer()
        );
    }

    public IndexedReadFiles read(Id id){
        return new IndexedReadFiles<>(fileIndex.scopeFor(id, false));
    }

    public IndexedReadWriteFiles readWrite(Id id){
        return new IndexedReadWriteFiles<>(fileIndex.scopeFor(id, true));
    }

    public IndexedReadFiles insight(Id id){
        return new IndexedReadFiles<>(fileIndex.scopeFor(id, true));
    }
}
