package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Function;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.GitFriendlyJSON.deserialize;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.GitFriendlyJSON.serialize;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString
@EqualsAndHashCode
@Slf4j
public class IndexFile {
    public static final String FILENAME = ".episode-index";

    @NonNull File workingCopy;

    @Getter(lazy = true)
    final File persistenceBackend = new File(workingCopy, FILENAME);

    @SneakyThrows
    @NonNull
    public Metadata getMetadata(){
        return deserialize(Files.readString(getPersistenceBackend().toPath()), Metadata.class);
    }

    @SneakyThrows
    public void setMetadata(@NonNull Metadata metadata){
        log.info("Set metadata: "+metadata);
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
