package com.github.filipmalczak.storyteller.api.storage;

import com.github.filipmalczak.storyteller.api.storage.envelope.FileEnvelope;

import java.util.Optional;

public interface FilesApi {
    FileEnvelope create(String id, String extension);

    default FileEnvelope create(String id){
        return create(id, "dat");
    }

    boolean exists(String id);

    void delete(String id);

    Optional<FileEnvelope> find(String id);
}
