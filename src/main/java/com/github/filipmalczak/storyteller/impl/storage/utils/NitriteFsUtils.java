package com.github.filipmalczak.storyteller.impl.storage.utils;

import com.github.filipmalczak.storyteller.impl.storage.NitriteStorageConfig;
import lombok.extern.flogger.Flogger;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Importer;

import java.io.File;
import java.util.Optional;

@Flogger
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
        var dump = getNitriteFile(config, id);
        log.atFine().log("Loading data from %s", dump);
        importer.importFrom(dump);
    }

    public static <Id extends Comparable<Id>> Nitrite load(NitriteStorageConfig<Id> config, Optional<Id> id){
        var out = makeNitrite(config);
        if (id.isPresent())
            populate(out, config, id.get());
        return out;
    }
}
