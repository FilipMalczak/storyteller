package com.github.filipmalczak.storyteller.impl.jgit.storage.data;

import com.github.filipmalczak.storyteller.api.storage.FilesApi;
import com.github.filipmalczak.storyteller.api.storage.envelope.FileEnvelope;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
final class Files implements FilesApi {
    @NonNull ManagedDir backend;

    @Override
    public FileEnvelope create(String id, String extension) {
        return new FileEnvelope(id, backend.create(id, extension));
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
    public Optional<FileEnvelope> find(String id) {
        return backend.find(id).map(f -> new FileEnvelope(id, f));
    }
}
