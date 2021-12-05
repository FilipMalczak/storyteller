package com.github.filipmalczak.storyteller.impl.jgit.storage.data;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.file.Files.write;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
final class ManagedDir {
    @NonNull File root;

    private File resolveWithExtension(String... pathComponents){
        File out = root;
        for (String component: pathComponents)
            out = new File(out, component);
        return out;
    }

    private Optional<File> resolveWithoutExtension(String... pathComponents){
        File dir = root;
        for (int i=0; i<pathComponents.length-1; ++i)
            dir = new File(dir, pathComponents[i]);
        String filename = pathComponents[pathComponents.length-1];
        var dirFiles = dir.listFiles();
        if (dirFiles == null)
            dirFiles = new File[0];
        var matching = Stream
            .of(dirFiles)
            .filter(x -> x.getName().startsWith(filename+"."))
            .toList();
        //todo can be optimized
        if (matching.size() == 0)
            return Optional.empty();
        if (matching.size() == 1)
            return matching.stream().findFirst();
        throw new RuntimeException(); //todo too many matching
    }

    @SneakyThrows
    public File create(String id, String ext) {
        if (exists(id))
            throw new RuntimeException();//todo
        File out = resolveWithExtension(id+"."+ext);
        out.getParentFile().mkdirs();
        out.createNewFile(); //checked if exists already, can ignore returned value
        return out;
    }

    public void delete(String id) {
        resolveWithoutExtension(id).get().delete(); //todo get may throw!
    }

    public Optional<File> find(String id) {
        return resolveWithoutExtension(id);
    }

    public boolean exists(String id) {
        return find(id).isPresent();
    }

    //todo exists and friends
    @SneakyThrows
    public void save(String id, byte[] payload) {
        //todo this could use FSUtils
        write(
            find(id)
                .get()
                .toPath()
            ,
            payload
        );
    }
}
