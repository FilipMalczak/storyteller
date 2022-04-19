package com.github.filipmalczak.storyteller.impl.storage;

import com.github.filipmalczak.storyteller.api.stack.task.TaskType;
import com.github.filipmalczak.storyteller.impl.stack.NitriteStackConfig;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Importer;

import java.io.File;
import java.util.Optional;

public class NitriteFsUtils {
    public static <Id extends Comparable<Id>> File getNitriteFile(NitriteStorageConfig<Id> config, Id id){
        return config
            .getDataStorage()
            .resolve("nosql")
            .resolve(config.getSerializer().toString(id))
            .resolve("data.json")
            .toFile();
    }

    public static Nitrite makeNitrite(NitriteStorageConfig<?> config){
        var builder = Nitrite.builder();
        if (config.isEnableNo2OffHeapStorage())
            builder = builder.enableOffHeapStorage();
        return builder.openOrCreate();
    }

    public static <Id extends Comparable<Id>> void populate(Nitrite nitrite, NitriteStorageConfig<Id> config, Id id){
        var importer = Importer.of(nitrite);
        importer.importFrom(getNitriteFile(config, id));
    }

    public static <Id extends Comparable<Id>> Nitrite load(NitriteStorageConfig<Id> config, Optional<Id> id){
        var out = makeNitrite(config);
        if (id.isPresent())
            populate(out, config, id.get());
        return out;
    }
}
