package com.github.filipmalczak.storyteller.impl.jgit.storage.data;

import com.github.filipmalczak.storyteller.api.storage.DocumentsApi;
import com.github.filipmalczak.storyteller.api.storage.envelope.DocumentEnvelope;
import com.github.filipmalczak.storyteller.impl.jgit.utils.GitFriendlyJSON;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.github.filipmalczak.storyteller.impl.jgit.utils.FSUtils.readFile;
import static com.github.filipmalczak.storyteller.impl.jgit.utils.FSUtils.writeFile;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
final class Documents implements DocumentsApi {
    @NonNull ManagedDir backend;

    @Override
    @SneakyThrows
    public <T> DocumentEnvelope<T> create(String id, T val) {
        var f = backend.create(id, ".json");
        writeFile(f, GitFriendlyJSON.serialize(val));
        return new DocumentEnvelope<>(id, val, (Class<T>) val.getClass());
    }

    @Override
    public boolean exists(String id) {
        return backend.exists(id);
    }

    @Override
    public void delete(String id) {
        backend.delete(id);
    }

    @Override
    @SneakyThrows
    public <T> Optional<DocumentEnvelope<T>> find(String id, Class<T> type) {
        return backend
            .find(id)
            .map(f -> GitFriendlyJSON.deserialize(readFile(f), type))
            .map(val -> new DocumentEnvelope(id, val, type));
    }

    @Override
    @SneakyThrows
    public <T> DocumentEnvelope<T> save(DocumentEnvelope<T> envelope) {
        backend
            .save(
                envelope.getId(),
                GitFriendlyJSON.serialize(envelope.getPayload()).getBytes(StandardCharsets.UTF_8)
            );
        return envelope;
    }
}
