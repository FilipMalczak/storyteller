package com.github.filipmalczak.storyteller.api.storage;

import com.github.filipmalczak.storyteller.api.storage.envelope.DocumentEnvelope;

import java.util.Optional;
import java.util.UUID;

public interface DocumentsApi {
    <T> DocumentEnvelope<T> create(String id, T val);

    default <T> DocumentEnvelope<T> create(T val){
        return create(UUID.randomUUID().toString(), val);
    }

    boolean exists(String id);

    void delete(String id);

    <T> Optional<DocumentEnvelope<T>> find(String id, Class<T> type);

    <T> DocumentEnvelope<T> save(DocumentEnvelope<T> envelope);
}
