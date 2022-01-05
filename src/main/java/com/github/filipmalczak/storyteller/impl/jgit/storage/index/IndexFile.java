package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.flogger.Flogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.GitFriendlyJSON.deserialize;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.GitFriendlyJSON.serialize;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString
@EqualsAndHashCode
@Flogger
public class IndexFile {
    public static final String FILENAME = ".episode-index";

    @NonNull File workingCopy;

    @Getter(lazy = true)
    final File persistenceBackend = new File(workingCopy, FILENAME);

    @NonNull
    public Optional<Metadata> findMetadata(){
        try {
            var out = Optional.of(deserialize(Files.readString(getPersistenceBackend().toPath()), Metadata.class));
            log.atFiner().log("Succesfully deserialized metadata %s from %s", out, persistenceBackend);
            return out;
        } catch (IOException | RuntimeException e){
            log.atFiner().withCause(e).log("Deserialization from %s failed", persistenceBackend);
            return Optional.empty();
        }
    }

    @SneakyThrows
    @NonNull
    public Metadata getMetadata(){
        return findMetadata().get();
    }

    @SneakyThrows
    public void setMetadata(@NonNull Metadata metadata){
        log.atFiner().log("Set metadata: "+metadata);
        Files.writeString(getPersistenceBackend().toPath(), serialize(metadata));
    }

    @NonNull
    public Metadata updateMetadata(@NonNull Function<Metadata, Metadata> action){
        var meta = getMetadata();
        var out = action.apply(meta);
        setMetadata(out);
        return out;
    }
}
