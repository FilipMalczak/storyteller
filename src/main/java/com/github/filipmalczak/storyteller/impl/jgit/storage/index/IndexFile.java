package com.github.filipmalczak.storyteller.impl.jgit.storage.index;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
public class IndexFile {
    public static final String FILENAME = ".episode-index";

    @NonNull File workingCopy;

    @Getter(lazy = true)
    final File persistenceBackend = new File(workingCopy, FILENAME);

    @SneakyThrows
    public Metadata getMetadata(){
        return deserialize(Files.readString(getPersistenceBackend().toPath()), Metadata.class);
    }

    @SneakyThrows
    public void setMetadata(Metadata metadata){
        Files.writeString(getPersistenceBackend().toPath(), serialize(metadata));
    }

    public Metadata updateMetadata(Function<Metadata, Metadata> action){
        var out = action.apply(getMetadata());
        setMetadata(out);
        return out;
    }
}
