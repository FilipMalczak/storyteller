package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.impl.stack.HistoryTracker;
import com.github.filipmalczak.storyteller.api.storage.files.ReadFilesApi;
import com.github.filipmalczak.storyteller.impl.storage.config.NitriteStorageConfig;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@Flogger
public class SimpleReadFiles<Id> implements ReadFilesApi {
    @NonNull NitriteStorageConfig<Id> config;
    @NonNull HistoryTracker<Id> tracker;
    @NonNull Id current;

    @Getter(lazy = true, value = AccessLevel.PROTECTED) private final Path filesPath = config.getDataStorage().resolve("files");

    //todo if I start tracking both the whole history as well as leaf history, I can browse leadfs only
    protected Stream<Path> readCandidates(Path path){
        return Stream.concat(Stream.of(current), tracker.get(current))
            .map(config.getSerializer()::toString)
            .map(serializedId -> getFilesPath().resolve(serializedId).resolve(path));
    }

    @Override
    public boolean exists(Path path) {
        return readCandidates(path).anyMatch(p -> p.toFile().exists());
    }

    @SneakyThrows
    private static InputStream newFileInputStream(File file){
        return new FileInputStream(file);
    }

    @Override
    @SneakyThrows
    public <T> T read(Path path, Function<InputStream, T> reader) {
        try (var stream = readCandidates(path)
            .peek(p -> log.atFine().log("Candidate for reading %s: %s", path, p))
            .map(Path::toFile)
            .filter(File::exists)
            .map(SimpleReadFiles::newFileInputStream)
            .findFirst()
            .get()){
            return reader.apply(stream);
        }
    }
}
