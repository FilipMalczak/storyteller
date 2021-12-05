package com.github.filipmalczak.storyteller.impl.jgit.storage.data;

import com.github.filipmalczak.storyteller.api.storage.PropertiesApi;
import com.github.filipmalczak.storyteller.api.storage.envelope.PropertyEnvelope;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.FSUtils.writeFile;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
final class Properties implements PropertiesApi {
    @NonNull ManagedDir backend;

    @Override
    @SneakyThrows
    public PropertyEnvelope create(String id, String val) {
        File f = backend.create(id, "val");
        writeFile(f, val);
        return new PropertyEnvelope(id, val);
    }

    @Override
    public boolean exists(String id) {
        return find(id).isPresent();
    }

    @Override
    public void delete(String id) {
        backend.delete(id);
    }

    @SneakyThrows
    private static String safeRead(File f){
        return readString(f.toPath());
    }

    @Override
    public Optional<PropertyEnvelope> find(String id) {
        return backend.find(id).map(f -> new PropertyEnvelope(id, safeRead(f)));
    }

    @Override
    @SneakyThrows
    public void save(PropertyEnvelope envelope) {
        backend
            .save(
                envelope
                    .getId(),
                envelope
                    .getPayload()
                    .getBytes(StandardCharsets.UTF_8)
            );
    }
}
